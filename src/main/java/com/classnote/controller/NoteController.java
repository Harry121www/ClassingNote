package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.*;
import com.classnote.entity.Note;
import com.classnote.service.NoteService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ApiResponse<NoteDetailResponse> create(@Valid @RequestBody NoteRequest req) {
        Note note = noteService.create(req);
        return ApiResponse.success("创建成功", NoteDetailResponse.from(note));
    }

    @GetMapping
    public ApiResponse<Page<NoteListResponse>> list(
            @RequestParam(required = false) String notebookId,
            @RequestParam(required = false) String tagId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort) {
        page = Math.max(0, page - 1);
        size = Math.min(size, 50);
        return ApiResponse.success(noteService.list(notebookId, tagId, keyword, page, size, sort)
                .map(NoteListResponse::from));
    }

    @GetMapping("/{id}")
    public ApiResponse<NoteDetailResponse> get(@PathVariable String id) {
        return ApiResponse.success(NoteDetailResponse.from(noteService.getById(id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<NoteDetailResponse> update(@PathVariable String id, @Valid @RequestBody NoteRequest req) {
        return ApiResponse.success(NoteDetailResponse.from(noteService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable String id) {
        noteService.delete(id);
        return ApiResponse.success("删除成功", null);
    }

    @DeleteMapping("/batch")
    public ApiResponse<DeleteResult> batchDelete(@Valid @RequestBody BatchDeleteRequest req) {
        DeleteResult result = noteService.batchDelete(req);
        return ApiResponse.success("已成功删除 " + result.getDeletedCount() + " 篇笔记", result);
    }
}
