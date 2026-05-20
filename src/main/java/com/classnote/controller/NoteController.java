package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.*;
import com.classnote.entity.Note;
import com.classnote.service.NoteService;
import com.classnote.llm.LLMService;
import com.classnote.llm.LLMServiceFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notes")
public class NoteController {

    private final NoteService noteService;
    private final LLMServiceFactory llmFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NoteController(NoteService noteService, LLMServiceFactory llmFactory) {
        this.noteService = noteService;
        this.llmFactory = llmFactory;
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

    @PostMapping(value = "/summarize", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter summarize(@Valid @RequestBody SummarizeRequest req) {
        SseEmitter emitter = new SseEmitter(300000L);

        List<Note> notes = noteService.getByIds(req.getNoteIds());
        if (notes.isEmpty()) {
            try {
                AnalysisEvent err = new AnalysisEvent("ERROR", null, "未找到选中的笔记", true);
                emitter.send(SseEmitter.event().name("analysis").data(objectMapper.writeValueAsString(err)));
                emitter.complete();
            } catch (IOException e) { emitter.completeWithError(e); }
            return emitter;
        }

        StringBuilder content = new StringBuilder();
        for (Note note : notes) {
            content.append("## ").append(note.getTitle()).append("\n\n")
                   .append(note.getContent()).append("\n\n---\n\n");
        }

        LLMService llm = llmFactory.getService();
        String prompt = String.format(
            "以下是几篇学习笔记。请将它们的内容进行整合归纳，生成一份阶段总结。\n\n" +
            "【重要规则】\n" +
            "- 只使用下面笔记中的内容，不要添加任何笔记中没有的知识、解释或补充\n" +
            "- 不要凭空编造笔记中不存在的信息\n" +
            "- 如果笔记内容互相矛盾，如实指出，不要自行判断对错\n" +
            "- 把相似主题的笔记合并归类，去除重复内容\n" +
            "- 保持原文的关键术语和表述，不要改写或润色\n" +
            "- 用 Markdown 格式，按主题分小节输出\n\n" +
            "笔记内容：\n\n%s", content.toString()
        );

        llm.streamChat("你是一个文档整理助手。你的唯一任务是将多篇笔记的内容合并归类，不添加任何原文没有的信息。请用中文。", prompt)
            .map(chunk -> new AnalysisEvent("SUMMARY", null, chunk, false))
            .concatWith(reactor.core.publisher.Mono.just(new AnalysisEvent("DONE", null, "", true)))
            .subscribe(
                event -> {
                    try {
                        emitter.send(SseEmitter.event().name("analysis")
                            .data(objectMapper.writeValueAsString(event)));
                    } catch (IOException e) { emitter.completeWithError(e); }
                },
                error -> {
                    try {
                        AnalysisEvent errEvent = new AnalysisEvent("ERROR", null,
                            error.getMessage() != null ? error.getMessage() : "总结失败", true);
                        emitter.send(SseEmitter.event().name("analysis")
                            .data(objectMapper.writeValueAsString(errEvent)));
                    } catch (IOException e) { }
                    emitter.complete();
                },
                emitter::complete
            );

        return emitter;
    }
}
