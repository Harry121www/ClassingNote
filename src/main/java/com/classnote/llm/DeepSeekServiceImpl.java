package com.classnote.llm;

import com.classnote.service.LLMConfigService;
import org.springframework.stereotype.Component;

@Component
public class DeepSeekServiceImpl extends OpenAIServiceImpl {

    public DeepSeekServiceImpl(LLMConfigService configService) {
        super(configService, "https://api.deepseek.com");
    }

    @Override
    public LLMProvider getProvider() {
        return LLMProvider.DEEPSEEK;
    }
}
