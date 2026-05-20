package com.classnote.llm;

import com.classnote.entity.LLMConfigEntity;
import com.classnote.service.LLMConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public class OpenAIServiceImpl implements LLMService {

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
            return Flux.error(new IllegalStateException(
                "未配置 Base URL，请在设置中填写 API 地址"));
        }

        Map<String, Object> body = Map.of(
            "model", config.getModel(),
            "stream", true,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            )
        );

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .build();

        return client.post()
                .uri("/v1/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> line.startsWith("data: ") && !line.startsWith("data: [DONE]"))
                .map(line -> {
                    try {
                        String json = line.substring(6);
                        JsonNode node = objectMapper.readTree(json);
                        JsonNode choices = node.get("choices");
                        if (choices != null && choices.size() > 0) {
                            JsonNode delta = choices.get(0).get("delta");
                            if (delta != null) {
                                JsonNode content = delta.get("content");
                                return content != null ? content.asText() : "";
                            }
                        }
                    } catch (Exception e) {
                        // skip parse errors on partial chunks
                    }
                    return "";
                });
    }
}
