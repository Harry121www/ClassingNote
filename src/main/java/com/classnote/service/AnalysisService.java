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

    public Flux<AnalysisEvent> analyze(List<FileItem> files, String projectName) {
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
                return Flux.just(new AnalysisEvent("ERROR", null, "No valid text files found", true));
            }

            Map<String, List<FileItem>> grouped = validFiles.stream()
                .collect(Collectors.groupingBy(f -> {
                    String path = f.getPath().replace('\\', '/');
                    int lastSlash = path.lastIndexOf('/');
                    return lastSlash > 0 ? path.substring(0, lastSlash) : "root";
                }));

            String project = projectName != null ? projectName : "the project";

            return Flux.concat(
                phasePerFile(llm, validFiles),
                phasePackage(llm, grouped),
                phaseOverview(llm, project, grouped.keySet())
            ).concatWith(Flux.just(new AnalysisEvent("DONE", null, "", true)));
        });
    }

    private Flux<AnalysisEvent> phasePerFile(LLMService llm, List<FileItem> files) {
        return Flux.fromIterable(files)
            .flatMap(file -> {
                String prompt = String.format(
                    "请分析以下源代码文件，用一段话概括它的职责、核心方法/函数，以及它和项目中其他部分的依赖关系。请用中文回答。\n\n" +
                    "文件: %s\n\n```\n%s\n```", file.getPath(), file.getContent()
                );
                return llm.streamChat(
                    "你是一个代码分析助手。请用中文提供简洁准确的源代码文件摘要。",
                    prompt
                ).map(chunk -> new AnalysisEvent("PER_FILE", file.getPath(), chunk, false))
                .concatWith(Mono.just(new AnalysisEvent("PER_FILE", file.getPath(), "", true)));
            }, 5);
    }

    private Flux<AnalysisEvent> phasePackage(LLMService llm, Map<String, List<FileItem>> grouped) {
        return Flux.fromIterable(grouped.entrySet())
            .concatMap(entry -> {
                String pkg = entry.getKey();
                String fileList = entry.getValue().stream()
                    .map(f -> "- " + f.getPath())
                    .collect(Collectors.joining("\n"));
                String prompt = String.format(
                    "以下文件属于同一个包/目录: %s\n\n%s\n\n" +
                    "请基于文件名（你之前已经分析过每个文件），为这个包生成结构化的知识点总结。内容包括：" +
                    "1) 包的职责，2) 关键类及其角色，3) 它们之间如何协作。请用中文回答。",
                    pkg, fileList
                );
                return llm.streamChat(
                    "你是一个软件架构分析师。请用中文清晰地总结包/模块的职责。",
                    prompt
                ).map(chunk -> new AnalysisEvent("PACKAGE", pkg, chunk, false))
                .concatWith(Mono.just(new AnalysisEvent("PACKAGE", pkg, "", true)));
            });
    }

    private Flux<AnalysisEvent> phaseOverview(LLMService llm, String projectName, Set<String> packages) {
        String pkgList = packages.stream().sorted()
            .map(p -> "- " + p)
            .collect(Collectors.joining("\n"));
        String prompt = String.format(
            "项目: %s\n\n包/模块列表:\n%s\n\n" +
            "你已经分析了这个项目的每个文件和包。现在请生成一份全面的项目总览（Markdown格式），包含：\n" +
            "## 项目架构\n- 整体架构模式\n- 代码中体现的技术栈\n\n" +
            "## 模块总览\n- 每个包/模块的简介\n\n" +
            "## 关键设计模式\n- 项目中使用的设计模式\n\n" +
            "## 数据流\n- 数据如何在系统中流转\n\n" +
            "请用中文回答。",
            projectName, pkgList
        );
        return llm.streamChat(
            "你是一个资深软件架构师。请用中文生成清晰、有教育意义的项目总览。",
            prompt
        ).map(chunk -> new AnalysisEvent("OVERVIEW", projectName, chunk, false))
        .concatWith(Mono.just(new AnalysisEvent("OVERVIEW", projectName, "", true)));
    }
}
