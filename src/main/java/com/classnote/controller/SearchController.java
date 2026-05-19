package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.NoteListResponse;
import com.classnote.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ApiResponse<Page<NoteListResponse>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String notebookId,
            @RequestParam(required = false) String tagId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ApiResponse.error(400, "关键词不能为空");
        }
        page = Math.max(0, page - 1);
        size = Math.min(size, 50);
        return ApiResponse.success(searchService.search(keyword, notebookId, tagId, page, size));
    }
}
