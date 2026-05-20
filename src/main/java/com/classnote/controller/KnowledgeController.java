package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.KnowledgeEntryRequest;
import com.classnote.dto.KnowledgeEntryResponse;
import com.classnote.service.KnowledgeService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @PostMapping
    public ApiResponse<KnowledgeEntryResponse> create(@Valid @RequestBody KnowledgeEntryRequest req) {
        return ApiResponse.success("知识库条目已创建",
            KnowledgeEntryResponse.from(knowledgeService.create(req)));
    }

    @GetMapping
    public ApiResponse<List<KnowledgeEntryResponse>> list(@RequestParam String project) {
        return ApiResponse.success(
            knowledgeService.listByProject(project).stream()
                .map(KnowledgeEntryResponse::from)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeEntryResponse> get(@PathVariable String id) {
        return ApiResponse.success(
            KnowledgeEntryResponse.from(knowledgeService.getById(id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<KnowledgeEntryResponse> update(@PathVariable String id,
                                                       @RequestBody KnowledgeEntryRequest req) {
        return ApiResponse.success("知识库条目已更新",
            KnowledgeEntryResponse.from(knowledgeService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable String id) {
        knowledgeService.delete(id);
        return ApiResponse.success("知识库条目已删除", null);
    }
}
