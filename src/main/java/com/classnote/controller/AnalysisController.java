package com.classnote.controller;

import com.classnote.dto.AnalysisEvent;
import com.classnote.dto.AnalysisRequest;
import com.classnote.dto.FileItem;
import com.classnote.service.AnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/pick-folder")
    public List<FileItem> pickFolder() throws Exception {
        CompletableFuture<File> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("选择项目文件夹");
            File dir = chooser.showDialog(null);
            future.complete(dir);
        });
        File dir = future.get();
        if (dir == null) return new ArrayList<>();

        List<FileItem> result = new ArrayList<>();
        Path rootPath = dir.toPath();
        collectFiles(dir, rootPath, result);
        return result;
    }

    private void collectFiles(File file, Path rootPath, List<FileItem> items) {
        if (file.isFile()) {
            try {
                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                String relPath = rootPath.relativize(file.toPath()).toString().replace('\\', '/');
                FileItem item = new FileItem();
                item.setPath(rootPath.getFileName() + "/" + relPath);
                item.setContent(content);
                items.add(item);
            } catch (IOException ignored) {
                // skip binary/unreadable files
            }
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    collectFiles(child, rootPath, items);
                }
            }
        }
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@Valid @RequestBody AnalysisRequest req) {
        SseEmitter emitter = new SseEmitter(300000L);

        analysisService.analyze(req.getFiles(), req.getProjectName(), req.getMode())
            .subscribe(
                event -> {
                    try {
                        String json = objectMapper.writeValueAsString(event);
                        emitter.send(SseEmitter.event().name("analysis").data(json));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    try {
                        AnalysisEvent errEvent = new AnalysisEvent("ERROR", null,
                            error.getMessage() != null ? error.getMessage() : "Analysis failed", true);
                        String json = objectMapper.writeValueAsString(errEvent);
                        emitter.send(SseEmitter.event().name("analysis").data(json));
                    } catch (IOException e) {
                        // ignore
                    }
                    emitter.complete();
                },
                emitter::complete
            );

        return emitter;
    }
}
