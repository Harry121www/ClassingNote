package com.classnote.llm;

import com.classnote.entity.LLMConfigEntity;
import com.classnote.service.LLMConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

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
                .retrieve()
                .bodyToFlux(String.class)
                .flatMap(chunk -> {
                    // A chunk may be one line, partial line, or multiple lines
                    return Flux.fromArray(chunk.split("\n", -1));
                })
                .map(line -> line.trim())
                .filter(line -> line.startsWith("data:") && line.length() > 5)
                .map(line -> line.substring(5).trim())
                .filter(payload -> !payload.isEmpty() && !payload.equals("[DONE]"))
                .handle((payload, sink) -> {
                    try {
                        JsonNode node = objectMapper.readTree(payload);
                        JsonNode choices = node.get("choices");
                        if (choices != null && choices.size() > 0) {
                            JsonNode delta = choices.get(0).get("delta");
                            if (delta != null) {
                                JsonNode content = delta.get("content");
                                if (content != null && !content.asText().isEmpty()) {
                                    sink.next(content.asText());
                                }
                            }
                        }
                    } catch (Exception e) {
                        // JSON may span chunks — discard, not much we can do
                    }
                });
    }
}
