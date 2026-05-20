package com.classnote.llm;

import com.classnote.service.LLMConfigService;
import org.springframework.stereotype.Component;

@Component
public class QwenServiceImpl extends OpenAIServiceImpl {

    public QwenServiceImpl(LLMConfigService configService) {
        super(configService, "https://dashscope.aliyuncs.com/compatible-mode");
    }

    @Override
    public LLMProvider getProvider() {
        return LLMProvider.QWEN;
    }
}
