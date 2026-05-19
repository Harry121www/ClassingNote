package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.DraftRequest;
import com.classnote.dto.DraftResponse;
import com.classnote.entity.Draft;
import com.classnote.service.DraftService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/drafts")
public class DraftController {

    private final DraftService draftService;

    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }

    @PostMapping
    public ApiResponse<DraftResponse> save(@Valid @RequestBody DraftRequest req) {
        Draft draft = draftService.save(req);
        return ApiResponse.success("草稿已保存", DraftResponse.from(draft));
    }

    @GetMapping("/latest")
    public ApiResponse<DraftResponse> getLatest() {
        Draft draft = draftService.getLatest();
        return ApiResponse.success(draft != null ? DraftResponse.from(draft) : null);
    }

    @DeleteMapping
    public ApiResponse<?> delete() {
        draftService.delete();
        return ApiResponse.success("草稿已清除", null);
    }
}
