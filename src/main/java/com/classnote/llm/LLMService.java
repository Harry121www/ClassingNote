package com.classnote.llm;

import reactor.core.publisher.Flux;

public interface LLMService {
    Flux<String> streamChat(String systemPrompt, String userPrompt);
    LLMProvider getProvider();
}
