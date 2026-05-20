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
                    "Analyze the following source file. In one paragraph, summarize its responsibility, " +
                    "key methods/functions, and its dependencies on other parts of the project.\n\n" +
                    "File: %s\n\n```\n%s\n```", file.getPath(), file.getContent()
                );
                return llm.streamChat(
                    "You are a code analysis assistant. Provide concise, accurate summaries of source code files.",
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
                    "The following files belong to the same package/directory: %s\n\n%s\n\n" +
                    "Based on these filenames (you've already analyzed each file), generate " +
                    "a structured knowledge summary for this package. Include: " +
                    "1) Package responsibility, 2) Key classes and their roles, 3) How they work together.",
                    pkg, fileList
                );
                return llm.streamChat(
                    "You are a software architecture analyst. Summarize packages/modules clearly.",
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
            "Project: %s\n\nPackages/Modules:\n%s\n\n" +
            "You have analyzed each file and package in this project. Now generate a comprehensive " +
            "project overview in Markdown format. Include:\n" +
            "## Project Architecture\n- Overall architecture pattern\n- Technology stack evident from the code\n\n" +
            "## Module Summary\n- Brief description of each package/module\n\n" +
            "## Key Design Patterns\n- Notable design patterns used\n\n" +
            "## Data Flow\n- How data moves through the system",
            projectName, pkgList
        );
        return llm.streamChat(
            "You are a senior software architect. Generate clear, educational project overviews.",
            prompt
        ).map(chunk -> new AnalysisEvent("OVERVIEW", projectName, chunk, false))
        .concatWith(Mono.just(new AnalysisEvent("OVERVIEW", projectName, "", true)));
    }
}
