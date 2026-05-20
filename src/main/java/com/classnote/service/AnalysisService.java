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
            "以下是一个项目的源代码文件。请从中提取所有**知识点和关键技术概念**，按类别整理。\n\n" +
            "要求：\n" +
            "1. 识别项目中使用的编程语言、框架、库\n" +
            "2. 提取核心知识点（如：设计模式、算法、数据结构、API设计、数据库操作等）\n" +
            "3. 每个知识点用一句话简要解释\n" +
            "4. 按类别分组（如：语言基础、框架使用、设计模式、工程实践等）\n" +
            "5. 用 Markdown 格式输出，每个知识点一行，格式：`- **知识点名称**: 一句话解释`\n\n" +
            "源代码：\n\n%s\n\n" +
            "请用中文回答。",
            allFiles.toString()
        );
        return llm.streamChat(
            "你是一个编程教学的助教。你需要从项目代码中提取知识点，帮助学生理解项目涉及的技术概念。请用中文回答。",
            prompt
        ).map(chunk -> new AnalysisEvent("KNOWLEDGE", project, chunk, false))
        .concatWith(Mono.just(new AnalysisEvent("KNOWLEDGE", project, "", true)));
    }

    private Flux<AnalysisEvent> suggestLearningPath(LLMService llm, String project,
                                                     Set<String> packages) {
        String pkgList = packages.stream().sorted().map(p -> "- " + p)
                .collect(Collectors.joining("\n"));
        String prompt = String.format(
            "项目: %s\n\n包含的模块:\n%s\n\n" +
            "你刚刚已经提取了这个项目的所有知识点。现在请在此基础上：\n\n" +
            "1. **知识体系总结**: 用一段话概括这个项目涵盖的技术领域\n" +
            "2. **进阶学习建议**: 列出 5-10 个学完这个项目后可以继续深入学习的方向或技术，每个附一句话说明\n" +
            "3. **相关资源推荐**: 推荐一些可以深入学习这些知识点的书籍、文档或在线课程（不需要具体书名，给出方向即可）\n\n" +
            "用 Markdown 格式输出，请用中文回答。",
            project, pkgList
        );
        return llm.streamChat(
            "你是一个编程教学的课程设计师。你需要根据项目涵盖的知识点，为学生规划后续学习路径。请用中文回答。",
            prompt
        ).map(chunk -> new AnalysisEvent("LEARNING", project, chunk, false))
        .concatWith(Mono.just(new AnalysisEvent("LEARNING", project, "", true)));
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
