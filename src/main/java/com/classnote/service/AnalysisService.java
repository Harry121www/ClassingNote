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
                return Flux.just(new AnalysisEvent("ERROR", null, "No valid text files found", true));
            }

            Map<String, List<FileItem>> grouped = validFiles.stream()
                .collect(Collectors.groupingBy(f -> {
                    String path = f.getPath().replace('\\', '/');
                    int lastSlash = path.lastIndexOf('/');
                    return lastSlash > 0 ? path.substring(0, lastSlash) : "root";
                }));

            String project = projectName != null ? projectName : "the project";

            if ("KNOWLEDGE".equalsIgnoreCase(mode)) {
                return knowledgeMode(llm, validFiles, project, grouped);
            }

            return Flux.concat(
                phasePerFile(llm, validFiles),
                phasePackage(llm, grouped),
                phaseOverview(llm, project, grouped.keySet())
            ).concatWith(Flux.just(new AnalysisEvent("DONE", null, "", true)));
        });
    }

    private Flux<AnalysisEvent> knowledgeMode(LLMService llm, List<FileItem> files,
                                               String project, Map<String, List<FileItem>> grouped) {
        return Flux.concat(
            extractKnowledgePoints(llm, files, project),
            suggestLearningPath(llm, project, grouped.keySet())
        ).concatWith(Flux.just(new AnalysisEvent("DONE", null, "", true)));
    }

    private Flux<AnalysisEvent> extractKnowledgePoints(LLMService llm, List<FileItem> files,
                                                        String project) {
        StringBuilder allFiles = new StringBuilder();
        for (FileItem f : files) {
            allFiles.append("### ").append(f.getPath()).append("\n```\n")
                    .append(f.getContent().length() > 2000
                        ? f.getContent().substring(0, 2000) + "\n... (truncated)"
                        : f.getContent())
                    .append("\n```\n\n");
        }

        String prompt = String.format(
            "以下是一个项目的源代码。请列出代码中**实际使用到的**技术和知识点。\n\n" +
            "规则：\n" +
            "1. 只列出代码中明确出现的技术（如导入的库、使用的注解、调用的API）\n" +
            "2. 不要列出代码中没有出现的技术或概念\n" +
            "3. 每个知识点标注它出现在哪个文件中\n" +
            "4. 按类别分组（如：框架、数据库、网络、安全等）\n\n" +
            "源代码：\n\n%s\n\n" +
            "用 Markdown 格式输出。请用中文。",
            allFiles.toString()
        );
        return llm.streamChat(
            "你是一个代码审查工具。只列出代码中实际使用的技术，不推测、不补充。请用中文。",
            prompt
        ).map(chunk -> new AnalysisEvent("KNOWLEDGE", project, chunk, false))
        .concatWith(Mono.just(new AnalysisEvent("KNOWLEDGE", project, "", true)));
    }

    private Flux<AnalysisEvent> suggestLearningPath(LLMService llm, String project,
                                                     Set<String> packages) {
        String pkgList = packages.stream().sorted().map(p -> "- " + p)
                .collect(Collectors.joining("\n"));
        String prompt = String.format(
            "项目: %s\n\n模块: %s\n\n" +
            "基于上面的知识点列表（这些知识点都来自实际代码），请：\n\n" +
            "1. 用一段话概括这个项目实际涉及的技术领域（只写已有的）\n" +
            "2. 基于项目中已经出现的技术，列出 3-5 个直接相关的进阶学习方向（每个一句话）\n\n" +
            "不要推荐项目中完全没有涉及的技术方向。用 Markdown 格式。请用中文。",
            project, pkgList
        );
        return llm.streamChat(
            "你是一个课程规划工具。只基于已有知识点推荐直接相关的学习方向，不凭空推荐。请用中文。",
            prompt
        ).map(chunk -> new AnalysisEvent("LEARNING", project, chunk, false))
        .concatWith(Mono.just(new AnalysisEvent("LEARNING", project, "", true)));
    }

    private Flux<AnalysisEvent> phasePerFile(LLMService llm, List<FileItem> files) {
        return Flux.fromIterable(files)
            .flatMap(file -> {
                String prompt = String.format(
                    "请阅读以下源代码文件，用一段话概括它的实际职责和对外接口。不要猜测或补充代码中没有的内容。\n\n" +
                    "文件: %s\n\n```\n%s\n```", file.getPath(), file.getContent()
                );
                return llm.streamChat(
                    "你是一个代码分析工具。只根据代码本身进行描述，不推测、不补充、不评价。请用中文。",
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
                    "以下文件属于同一个包/目录: %s\n\n文件列表:\n%s\n\n" +
                    "根据文件名和之前每个文件的单独分析，归纳这个包的整体职责。只描述代码中实际存在的结构，不添加推测。请用中文。",
                    pkg, fileList
                );
                return llm.streamChat(
                    "你是一个代码结构分析工具。只根据给定的代码事实进行归纳，不编造、不补充。请用中文。",
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
            "项目: %s\n\n包含的模块: %s\n\n" +
            "基于之前对每个文件和包的逐层分析，汇总生成项目总览（Markdown格式）。只写代码中实际存在的内容：\n" +
            "## 项目结构\n- 目录/包的组织方式\n\n" +
            "## 模块职责\n- 每个包的实际功能\n\n" +
            "## 技术栈\n- 代码中实际使用的框架和库\n\n" +
            "不要添加代码中不存在的架构模式或设计模式。请用中文。",
            projectName, pkgList
        );
        return llm.streamChat(
            "你是一个项目文档生成工具。只根据代码事实生成总览，不推测、不补充。请用中文。",
            prompt
        ).map(chunk -> new AnalysisEvent("OVERVIEW", projectName, chunk, false))
        .concatWith(Mono.just(new AnalysisEvent("OVERVIEW", projectName, "", true)));
    }
}
