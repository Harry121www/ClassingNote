package com.classnote.llm;

import com.classnote.entity.LLMConfigEntity;
import com.classnote.service.LLMConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

@Component
public class OllamaServiceImpl implements LLMService {

    private final LLMConfigService configService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OllamaServiceImpl(LLMConfigService configService) {
        this.configService = configService;
    }

    @Override
    public LLMProvider getProvider() {
        return LLMProvider.OLLAMA;
    }

    @Override
    public Flux<String> streamChat(String systemPrompt, String userPrompt) {
        LLMConfigEntity config = configService.get();
        String baseUrl = config.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:11434";
        }

        String combinedPrompt = systemPrompt + "\n\n" + userPrompt;

        Map<String, Object> body = Map.of(
            "model", config.getModel(),
            "prompt", combinedPrompt,
            "stream", true
        );

        WebClient client = WebClient.builder().baseUrl(baseUrl).build();

        return client.post()
                .uri("/api/generate")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> !line.isBlank())
                .map(line -> {
                    try {
                        JsonNode node = objectMapper.readTree(line);
                        JsonNode response = node.get("response");
                        return response != null ? response.asText() : "";
                    } catch (Exception e) {
                        return "";
                    }
                });
    }
}
