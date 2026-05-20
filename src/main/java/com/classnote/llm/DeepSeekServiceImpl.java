package com.classnote.llm;

import com.classnote.service.LLMConfigService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class DeepSeekServiceImpl implements LLMService {

    private final OpenAIServiceImpl openAI;

    public DeepSeekServiceImpl(LLMConfigService configService) {
        this.openAI = new OpenAIServiceImpl(configService);
    }

    @Override
    public LLMProvider getProvider() {
        return LLMProvider.DEEPSEEK;
    }

    @Override
    public Flux<String> streamChat(String systemPrompt, String userPrompt) {
        return openAI.streamChat(systemPrompt, userPrompt);
    }
}
