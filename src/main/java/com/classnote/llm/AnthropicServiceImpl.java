package com.classnote.llm;

import com.classnote.entity.LLMConfigEntity;
import com.classnote.service.LLMConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Component
public class AnthropicServiceImpl implements LLMService {

    private final LLMConfigService configService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnthropicServiceImpl(LLMConfigService configService) {
        this.configService = configService;
    }

    @Override
    public LLMProvider getProvider() {
        return LLMProvider.ANTHROPIC;
    }

    @Override
    public Flux<String> streamChat(String systemPrompt, String userPrompt) {
        LLMConfigEntity config = configService.get();
        String baseUrl = config.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.anthropic.com";
        }

        Map<String, Object> body = Map.of(
            "model", config.getModel(),
            "max_tokens", 4096,
            "system", systemPrompt,
            "stream", true,
            "messages", List.of(
                Map.of("role", "user", "content", userPrompt)
            )
        );

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", config.getApiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();

        return client.post()
                .uri("/v1/messages")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> line.startsWith("data: "))
                .map(line -> {
                    try {
                        String json = line.substring(6);
                        JsonNode node = objectMapper.readTree(json);
                        JsonNode type = node.get("type");
                        if ("content_block_delta".equals(type != null ? type.asText() : "")) {
                            JsonNode delta = node.get("delta");
                            if (delta != null) {
                                JsonNode text = delta.get("text");
                                return text != null ? text.asText() : "";
                            }
                        }
                    } catch (Exception e) {
                        // skip parse errors
                    }
                    return "";
                });
    }
}
