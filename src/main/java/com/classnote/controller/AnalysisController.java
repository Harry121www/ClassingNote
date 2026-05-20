package com.classnote.controller;

import com.classnote.dto.AnalysisEvent;
import com.classnote.dto.AnalysisRequest;
import com.classnote.service.AnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@Valid @RequestBody AnalysisRequest req) {
        SseEmitter emitter = new SseEmitter(300000L);

        analysisService.analyze(req.getFiles(), req.getProjectName())
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
