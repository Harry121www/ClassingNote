package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.NotebookRequest;
import com.classnote.dto.NotebookResponse;
import com.classnote.entity.Notebook;
import com.classnote.service.NotebookService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/notebooks")
public class NotebookController {

    private final NotebookService notebookService;

    public NotebookController(NotebookService notebookService) {
        this.notebookService = notebookService;
    }

    @PostMapping
    public ApiResponse<NotebookResponse> create(@Valid @RequestBody NotebookRequest req) {
        Notebook nb = notebookService.create(req);
        return ApiResponse.success("创建成功", NotebookResponse.from(nb));
    }

    @GetMapping
    public ApiResponse<Page<NotebookResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort) {
        page = Math.max(0, page - 1);
        size = Math.min(size, 50);
        return ApiResponse.success(notebookService.list(page, size, sort).map(NotebookResponse::from));
    }

    @GetMapping("/{id}")
    public ApiResponse<NotebookResponse> get(@PathVariable String id) {
        return ApiResponse.success(NotebookResponse.from(notebookService.getById(id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<NotebookResponse> update(@PathVariable String id, @Valid @RequestBody NotebookRequest req) {
        return ApiResponse.success(NotebookResponse.from(notebookService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable String id) {
        int count = notebookService.delete(id);
        return ApiResponse.success("删除成功，已同时删除 " + count + " 篇笔记", null);
    }
}
