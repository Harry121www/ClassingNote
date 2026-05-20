package com.classnote.llm;

import com.classnote.entity.LLMConfigEntity;
import com.classnote.service.LLMConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OpenAIServiceImpl implements LLMService {

    private static final Logger log = LoggerFactory.getLogger(OpenAIServiceImpl.class);

    private final LLMConfigService configService;
    private final String defaultBaseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAIServiceImpl(LLMConfigService configService) {
        this(configService, "https://api.openai.com");
    }

    protected OpenAIServiceImpl(LLMConfigService configService, String defaultBaseUrl) {
        this.configService = configService;
        this.defaultBaseUrl = defaultBaseUrl;
    }

    @Override
    public LLMProvider getProvider() {
        return LLMProvider.OPENAI;
    }

    @Override
    public Flux<String> streamChat(String systemPrompt, String userPrompt) {
        LLMConfigEntity config = configService.get();
        String baseUrl = config.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = this.defaultBaseUrl;
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            return Flux.error(new IllegalStateException("未配置 Base URL"));
        }
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            return Flux.error(new IllegalStateException("未配置 API Key"));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", config.getModel());
        body.put("stream", true);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));
        body.put("messages", messages);

        log.info("LLM request to {} model {}", baseUrl, config.getModel());

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();

        return client.post()
                .uri("/v1/chat/completions")
                .bodyValue(body)
                .exchangeToFlux(response -> {
                    log.info("LLM response status: {}", response.statusCode());
                    if (response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
                        return response.bodyToMono(String.class)
                            .flatMapMany(errorBody -> {
                                log.error("LLM API error: {}", errorBody);
                                return Flux.error(new IllegalStateException(
                                    "API " + response.statusCode().value() + ": " + errorBody));
                            });
                    }
                    return response.bodyToFlux(DataBuffer.class)
                            .map(buffer -> {
                                byte[] bytes = new byte[buffer.readableByteCount()];
                                buffer.read(bytes);
                                DataBufferUtils.release(buffer);
                                return new String(bytes, StandardCharsets.UTF_8);
                            });
                })
                .flatMap(chunk -> {
                    String[] lines = chunk.split("\n");
                    return Flux.fromArray(lines);
                })
                .map(line -> line.replace("\r", "").trim())
                .filter(line -> !line.isEmpty())
                .doOnNext(line -> log.debug("SSE line: {}", line))
                .filter(line -> line.startsWith("data:"))
                .map(line -> {
                    String payload = line.substring(5).trim();
                    if (payload.equals("[DONE]")) return "";
                    try {
                        JsonNode node = objectMapper.readTree(payload);
                        JsonNode choices = node.get("choices");
                        if (choices != null && choices.size() > 0) {
                            JsonNode delta = choices.get(0).get("delta");
                            if (delta != null) {
                                JsonNode content = delta.get("content");
                                if (content != null) {
                                    String text = content.asText();
                                    if (!text.isEmpty()) {
                                        log.debug("LLM text: {}", text);
                                        return text;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse SSE json: {}", payload);
                    }
                    return "";
                })
                .filter(text -> !text.isEmpty());
    }
}
