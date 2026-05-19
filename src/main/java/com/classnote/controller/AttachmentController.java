package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.AttachmentResponse;
import com.classnote.entity.Attachment;
import com.classnote.service.AttachmentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping("/attachments/upload")
    public ApiResponse<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("noteId") String noteId) throws IOException {
        Attachment att = attachmentService.upload(file, noteId);
        Map<String, Object> data = new HashMap<>();
        data.put("id", att.getId());
        data.put("fileName", att.getFileName());
        data.put("filePath", att.getFilePath());
        data.put("fileSize", att.getFileSize());
        data.put("mimeType", att.getMimeType());
        String relativePath = att.getFilePath().replace("\\", "/");
        int idx = relativePath.indexOf("classnote-files");
        if (idx >= 0) {
            relativePath = relativePath.substring(idx + 16);
        }
        data.put("markdownRef", "![" + att.getFileName() + "](attachments/" + relativePath + ")");
        return ApiResponse.success("上传成功", data);
    }

    @GetMapping("/notes/{noteId}/attachments")
    public ApiResponse<List<AttachmentResponse>> listByNote(@PathVariable String noteId) {
        return ApiResponse.success(attachmentService.listByNote(noteId).stream()
                .map(AttachmentResponse::from).collect(Collectors.toList()));
    }

    @DeleteMapping("/attachments/{id}")
    public ApiResponse<?> delete(@PathVariable String id) {
        attachmentService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
}
