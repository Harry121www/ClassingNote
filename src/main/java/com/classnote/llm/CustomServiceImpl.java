package com.classnote.llm;

import com.classnote.service.LLMConfigService;
import org.springframework.stereotype.Component;

@Component
public class CustomServiceImpl extends OpenAIServiceImpl {

    public CustomServiceImpl(LLMConfigService configService) {
        super(configService, "");
    }

    @Override
    public LLMProvider getProvider() {
        return LLMProvider.CUSTOM;
    }
}
