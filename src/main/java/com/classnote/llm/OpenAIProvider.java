package com.classnote.llm;

import com.classnote.service.LLMConfigService;
import org.springframework.stereotype.Component;

@Component
public class OpenAIProvider extends OpenAIServiceImpl {

    public OpenAIProvider(LLMConfigService configService) {
        super(configService, "https://api.openai.com");
    }

    @Override
    public LLMProvider getProvider() {
        return LLMProvider.OPENAI;
    }
}
