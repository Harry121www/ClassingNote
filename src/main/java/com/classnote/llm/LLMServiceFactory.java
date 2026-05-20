package com.classnote.llm;

import com.classnote.entity.LLMConfigEntity;
import com.classnote.service.LLMConfigService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LLMServiceFactory {

    private final Map<LLMProvider, LLMService> serviceMap;
    private final LLMConfigService configService;

    public LLMServiceFactory(List<LLMService> services, LLMConfigService configService) {
        this.serviceMap = services.stream()
                .collect(Collectors.toMap(LLMService::getProvider, Function.identity()));
        this.configService = configService;
    }

    public LLMService getService() {
        LLMConfigEntity config = configService.get();
        LLMProvider provider = LLMProvider.valueOf(config.getProvider());
        LLMService service = serviceMap.get(provider);
        if (service == null) {
            throw new IllegalStateException("No LLM service registered for provider: " + provider);
        }
        return service;
    }
}
