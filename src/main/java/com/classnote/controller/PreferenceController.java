package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.PreferenceRequest;
import com.classnote.entity.UserPreference;
import com.classnote.service.PreferenceService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/preferences")
public class PreferenceController {

    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping
    public ApiResponse<UserPreference> get() {
        return ApiResponse.success(preferenceService.get());
    }

    @PutMapping
    public ApiResponse<UserPreference> update(@RequestBody PreferenceRequest req) {
        return ApiResponse.success(preferenceService.update(req));
    }
}
