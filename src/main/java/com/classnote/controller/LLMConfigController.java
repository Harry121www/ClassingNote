package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.LLMConfigRequest;
import com.classnote.dto.LLMConfigResponse;
import com.classnote.service.LLMConfigService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/llm")
public class LLMConfigController {

    private final LLMConfigService configService;

    public LLMConfigController(LLMConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/config")
    public ApiResponse<LLMConfigResponse> getConfig() {
        return ApiResponse.success(LLMConfigResponse.from(configService.get()));
    }

    @PutMapping("/config")
    public ApiResponse<LLMConfigResponse> updateConfig(@Valid @RequestBody LLMConfigRequest req) {
        return ApiResponse.success("配置已更新", LLMConfigResponse.from(configService.update(req)));
    }
}
