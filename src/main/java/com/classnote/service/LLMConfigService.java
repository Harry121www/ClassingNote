package com.classnote.service;

import com.classnote.dto.LLMConfigRequest;
import com.classnote.entity.LLMConfigEntity;
import com.classnote.repository.LLMConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LLMConfigService {

    private final LLMConfigRepository repo;

    public LLMConfigService(LLMConfigRepository repo) {
        this.repo = repo;
    }

    public LLMConfigEntity get() {
        return repo.findById("default").orElseGet(() -> repo.save(new LLMConfigEntity()));
    }

    @Transactional
    public LLMConfigEntity update(LLMConfigRequest req) {
        LLMConfigEntity config = get();
        config.setProvider(req.getProvider());
        if (req.getApiKey() != null && !req.getApiKey().isBlank()) {
            config.setApiKey(req.getApiKey());
        }
        if (req.getBaseUrl() != null) {
            config.setBaseUrl(req.getBaseUrl());
        }
        config.setModel(req.getModel());
        return repo.save(config);
    }
}
