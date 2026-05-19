package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.ExportResponse;
import com.classnote.service.ExportService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/notes/{id}")
    public ApiResponse<ExportResponse> exportNote(
            @PathVariable String id,
            @RequestParam String targetDir) throws IOException {
        return ApiResponse.success("导出成功", exportService.exportNote(id, targetDir));
    }

    @GetMapping("/notebooks/{id}")
    public ApiResponse<ExportResponse> exportNotebook(
            @PathVariable String id,
            @RequestParam String targetDir) throws IOException {
        return ApiResponse.success("导出成功", exportService.exportNotebook(id, targetDir));
    }
}
