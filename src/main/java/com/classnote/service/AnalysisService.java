package com.classnote.service;

import com.classnote.dto.AnalysisEvent;
import com.classnote.dto.FileItem;
import com.classnote.llm.LLMService;
import com.classnote.llm.LLMServiceFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private final LLMServiceFactory llmFactory;

    private static final Set<String> TEXT_EXTENSIONS = Set.of(
        ".java", ".py", ".js", ".ts", ".jsx", ".tsx", ".vue", ".kt", ".swift",
        ".go", ".rs", ".c", ".cpp", ".cs", ".rb", ".php", ".scala",
        ".xml", ".yml", ".yaml", ".json", ".properties", ".gradle",
        ".md", ".txt", ".css", ".scss", ".html", ".sql"
    );

    public AnalysisService(LLMServiceFactory llmFactory) {
        this.llmFactory = llmFactory;
    }

    public Flux<AnalysisEvent> analyze(List<FileItem> files, String projectName, String mode) {
        return Flux.defer(() -> {
            LLMService llm = llmFactory.getService();

            List<FileItem> validFiles = files.stream()
                .filter(f -> {
                    String name = f.getPath().toLowerCase();
                    for (String ext : TEXT_EXTENSIONS) {
                        if (name.endsWith(ext)) return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());

            if (validFiles.isEmpty()) {
                return Flux.just(new AnalysisEvent("ERROR", null, "没有找到有效文件", true));
            }

            String project = projectName != null ? projectName : "这个项目";

            StringBuilder code = new StringBuilder();
            for (FileItem f : validFiles) {
                code.append("### ").append(f.getPath()).append("\n```\n")
                    .append(f.getContent().length() > 3000
                        ? f.getContent().substring(0, 3000) + "\n... (省略)"
                        : f.getContent())
                    .append("\n```\n\n");
            }

            String prompt;
            String systemPrompt;
            String phase;

            if ("KNOWLEDGE".equalsIgnoreCase(mode)) {
                phase = "KNOWLEDGE";
                systemPrompt = "你是一个编程老师。请帮学生从项目代码中提取学习知识点。用简洁清晰的 Markdown 格式输出。";
                prompt = String.format(
                    "项目: %s\n\n源代码:\n%s\n" +
                    "请从这个项目中提取学生应该学习的技术知识点。按以下结构输出：\n\n" +
                    "## 技术栈\n列出项目中实际使用的编程语言、框架和库（只写代码中出现的）\n\n" +
                    "## 核心知识点\n列出项目中体现的技术概念，每行一个，格式：- **概念名**: 出自哪个文件，简要说明\n\n" +
                    "## 学习建议\n基于这个项目的内容，建议 3-5 个可以继续学习的方向",
                    project, code
                );
            } else {
                phase = "ANALYSIS";
                systemPrompt = "你是一个编程老师。请帮学生理解这个项目的代码结构和功能。用简洁清晰的 Markdown 格式输出。";
                prompt = String.format(
                    "项目: %s\n\n源代码:\n%s\n" +
                    "请分析这个项目的代码，用中文按以下结构输出：\n\n" +
                    "## 项目概述\n用一段话概括这个项目的功能\n\n" +
                    "## 文件说明\n逐个文件说明其作用，格式：- `文件名`: 做什么的\n\n" +
                    "## 技术栈\n代码中实际用到的框架、库、注解\n\n" +
                    "## 代码结构\n项目目录和包的层次结构",
                    project, code
                );
            }

            return llm.streamChat(systemPrompt, prompt)
                .map(chunk -> new AnalysisEvent(phase, project, chunk, false))
                .concatWith(Mono.just(new AnalysisEvent("DONE", null, "", true)));
        });
    }
}
