# Project Analysis Feature — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add drag-and-drop project folder analysis via configurable LLM API, generating layered knowledge points savable as notes or knowledge base entries.

**Architecture:** Frontend File API reads folder → user selects files in tree → POST to backend → backend streams SSE via three-phase LLM prompting → frontend renders progressive Markdown → user saves as Note or KnowledgeEntry.

**Tech Stack:** Spring Boot 2.7.18 (WebFlux for streaming), Vue 3 + Element Plus + Pinia, SSE (Server-Sent Events)

---

### Task 1: Add WebFlux dependency and LLM config entity

**Files:**
- Modify: `pom.xml`
- Create: `src/main/java/com/classnote/entity/LLMConfigEntity.java`
- Create: `src/main/java/com/classnote/repository/LLMConfigRepository.java`

- [ ] **Step 1: Add spring-boot-starter-webflux to pom.xml**

Replace the dependencies section in `pom.xml` — add webflux dependency after validation:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

- [ ] **Step 2: Create LLMConfigEntity**

```java
package com.classnote.entity;

import javax.persistence.*;

@Entity
@Table(name = "llm_config")
public class LLMConfigEntity {

    @Id
    @Column(length = 36)
    private String id = "default";

    @Column(nullable = false, length = 50)
    private String provider = "OPENAI";

    @Column(length = 500)
    private String apiKey;

    @Column(length = 500)
    private String baseUrl;

    @Column(length = 100)
    private String model = "gpt-4o";

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
```

- [ ] **Step 3: Create LLMConfigRepository**

```java
package com.classnote.repository;

import com.classnote.entity.LLMConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LLMConfigRepository extends JpaRepository<LLMConfigEntity, String> {
}
```

- [ ] **Step 4: Commit**

```bash
git add pom.xml src/main/java/com/classnote/entity/LLMConfigEntity.java src/main/java/com/classnote/repository/LLMConfigRepository.java
git commit -m "feat: add webflux dependency and LLM config entity"
```

---

### Task 2: LLMConfigService — config CRUD

**Files:**
- Create: `src/main/java/com/classnote/service/LLMConfigService.java`
- Create: `src/main/java/com/classnote/dto/LLMConfigRequest.java`
- Create: `src/main/java/com/classnote/dto/LLMConfigResponse.java`

- [ ] **Step 1: Create LLMConfigRequest DTO**

```java
package com.classnote.dto;

import javax.validation.constraints.NotBlank;

public class LLMConfigRequest {
    @NotBlank
    private String provider;
    private String apiKey;
    private String baseUrl;
    @NotBlank
    private String model;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
```

- [ ] **Step 2: Create LLMConfigResponse DTO (masks API key)**

```java
package com.classnote.dto;

import com.classnote.entity.LLMConfigEntity;

public class LLMConfigResponse {
    private String provider;
    private String apiKey;    // masked
    private String baseUrl;
    private String model;

    public static LLMConfigResponse from(LLMConfigEntity entity) {
        LLMConfigResponse r = new LLMConfigResponse();
        r.provider = entity.getProvider();
        r.apiKey = maskKey(entity.getApiKey());
        r.baseUrl = entity.getBaseUrl();
        r.model = entity.getModel();
        return r;
    }

    private static String maskKey(String key) {
        if (key == null || key.length() <= 8) return "****";
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
```

- [ ] **Step 3: Create LLMConfigService**

```java
package com.classnote.service;

import com.classnote.dto.LLMConfigRequest;
import com.classnote.entity.LLMConfigEntity;
import com.classnote.repository.LLMConfigRepository;
import org.springframework.stereotype.Service;

@Service
public class LLMConfigService {

    private final LLMConfigRepository repo;

    public LLMConfigService(LLMConfigRepository repo) {
        this.repo = repo;
    }

    public LLMConfigEntity get() {
        return repo.findById("default").orElseGet(() -> repo.save(new LLMConfigEntity()));
    }

    public LLMConfigEntity update(LLMConfigRequest req) {
        LLMConfigEntity config = get();
        config.setProvider(req.getProvider());
        if (req.getApiKey() != null && !req.getApiKey().isBlank()) {
            config.setApiKey(req.getApiKey());
        }
        if (req.getBaseUrl() != null) {
            config.setBaseUrl(req.getBaseUrl());
        }
        config.setModel(req.getModel());
        return repo.save(config);
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/classnote/service/LLMConfigService.java src/main/java/com/classnote/dto/LLMConfigRequest.java src/main/java/com/classnote/dto/LLMConfigResponse.java
git commit -m "feat: add LLM config service with CRUD"
```

---

### Task 3: LLM controller — config endpoints

**Files:**
- Create: `src/main/java/com/classnote/controller/LLMConfigController.java`

- [ ] **Step 1: Create LLMConfigController**

```java
package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.LLMConfigRequest;
import com.classnote.dto.LLMConfigResponse;
import com.classnote.service.LLMConfigService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/llm")
public class LLMConfigController {

    private final LLMConfigService configService;

    public LLMConfigController(LLMConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/config")
    public ApiResponse<LLMConfigResponse> getConfig() {
        return ApiResponse.success(LLMConfigResponse.from(configService.get()));
    }

    @PutMapping("/config")
    public ApiResponse<LLMConfigResponse> updateConfig(@Valid @RequestBody LLMConfigRequest req) {
        return ApiResponse.success("配置已更新", LLMConfigResponse.from(configService.update(req)));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/classnote/controller/LLMConfigController.java
git commit -m "feat: add LLM config REST endpoints"
```

---

### Task 4: LLM service abstraction — interface and factory

**Files:**
- Create: `src/main/java/com/classnote/llm/LLMProvider.java`
- Create: `src/main/java/com/classnote/llm/LLMService.java`
- Create: `src/main/java/com/classnote/llm/LLMServiceFactory.java`

- [ ] **Step 1: Create LLMProvider enum**

```java
package com.classnote.llm;

public enum LLMProvider {
    OPENAI,
    ANTHROPIC,
    OLLAMA,
    DEEPSEEK,
    QWEN,
    CUSTOM
}
```

- [ ] **Step 2: Create LLMService interface**

```java
package com.classnote.llm;

import reactor.core.publisher.Flux;

public interface LLMService {
    Flux<String> streamChat(String systemPrompt, String userPrompt);
    LLMProvider getProvider();
}
```

- [ ] **Step 3: Create LLMServiceFactory**

```java
package com.classnote.llm;

import com.classnote.entity.LLMConfigEntity;
import com.classnote.service.LLMConfigService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LLMServiceFactory {

    private final Map<LLMProvider, LLMService> serviceMap;
    private final LLMConfigService configService;

    public LLMServiceFactory(List<LLMService> services, LLMConfigService configService) {
        this.serviceMap = services.stream()
                .collect(Collectors.toMap(LLMService::getProvider, Function.identity()));
        this.configService = configService;
    }

    public LLMService getService() {
        LLMConfigEntity config = configService.get();
        LLMProvider provider = LLMProvider.valueOf(config.getProvider());
        LLMService service = serviceMap.get(provider);
        if (service == null) {
            throw new IllegalStateException("No LLM service registered for provider: " + provider);
        }
        return service;
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/classnote/llm/LLMProvider.java src/main/java/com/classnote/llm/LLMService.java src/main/java/com/classnote/llm/LLMServiceFactory.java
git commit -m "feat: add LLM service interface and factory"
```

---

### Task 5: OpenAI-compatible LLM service implementation

**Files:**
- Create: `src/main/java/com/classnote/llm/OpenAIServiceImpl.java`

- [ ] **Step 1: Create OpenAIServiceImpl**

```java
package com.classnote.llm;

import com.classnote.entity.LLMConfigEntity;
import com.classnote.service.LLMConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Component
public class OpenAIServiceImpl implements LLMService {

    private final LLMConfigService configService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAIServiceImpl(LLMConfigService configService) {
        this.configService = configService;
    }

    @Override
    public LLMProvider getProvider() {
        return LLMProvider.OPENAI;
    }

    @Override
    public Flux<String> streamChat(String systemPrompt, String userPrompt) {
        LLMConfigEntity config = configService.get();
        String baseUrl = config.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.openai.com";
        }
        String url = baseUrl + "/v1/chat/completions";

        Map<String, Object> body = Map.of(
            "model", config.getModel(),
            "stream", true,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            )
        );

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .build();

        return client.post()
                .uri("/v1/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> line.startsWith("data: ") && !line.startsWith("data: [DONE]"))
                .map(line -> {
                    try {
                        String json = line.substring(6);
                        JsonNode node = objectMapper.readTree(json);
                        JsonNode choices = node.get("choices");
                        if (choices != null && choices.size() > 0) {
                            JsonNode delta = choices.get(0).get("delta");
                            if (delta != null) {
                                JsonNode content = delta.get("content");
                                return content != null ? content.asText() : "";
                            }
                        }
                    } catch (Exception e) {
                        // skip parse errors on partial chunks
                    }
                    return "";
                });
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/classnote/llm/OpenAIServiceImpl.java
git commit -m "feat: add OpenAI-compatible LLM service impl"
```

---

### Task 6: Anthropic LLM service implementation

**Files:**
- Create: `src/main/java/com/classnote/llm/AnthropicServiceImpl.java`

- [ ] **Step 1: Create AnthropicServiceImpl**

```java
package com.classnote.llm;

import com.classnote.entity.LLMConfigEntity;
import com.classnote.service.LLMConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Component
public class AnthropicServiceImpl implements LLMService {

    private final LLMConfigService configService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnthropicServiceImpl(LLMConfigService configService) {
        this.configService = configService;
    }

    @Override
    public LLMProvider getProvider() {
        return LLMProvider.ANTHROPIC;
    }

    @Override
    public Flux<String> streamChat(String systemPrompt, String userPrompt) {
        LLMConfigEntity config = configService.get();
        String baseUrl = config.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.anthropic.com";
        }

        Map<String, Object> body = Map.of(
            "model", config.getModel(),
            "max_tokens", 4096,
            "system", systemPrompt,
            "stream", true,
            "messages", List.of(
                Map.of("role", "user", "content", userPrompt)
            )
        );

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", config.getApiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();

        return client.post()
                .uri("/v1/messages")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> line.startsWith("data: "))
                .map(line -> {
                    try {
                        String json = line.substring(6);
                        JsonNode node = objectMapper.readTree(json);
                        JsonNode type = node.get("type");
                        if ("content_block_delta".equals(type != null ? type.asText() : "")) {
                            JsonNode delta = node.get("delta");
                            if (delta != null) {
                                JsonNode text = delta.get("text");
                                return text != null ? text.asText() : "";
                            }
                        }
                    } catch (Exception e) {
                        // skip parse errors
                    }
                    return "";
                });
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/classnote/llm/AnthropicServiceImpl.java
git commit -m "feat: add Anthropic LLM service impl"
```

---

### Task 7: Ollama LLM service implementation

**Files:**
- Create: `src/main/java/com/classnote/llm/OllamaServiceImpl.java`

- [ ] **Step 1: Create OllamaServiceImpl**

```java
package com.classnote.llm;

import com.classnote.entity.LLMConfigEntity;
import com.classnote.service.LLMConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

@Component
public class OllamaServiceImpl implements LLMService {

    private final LLMConfigService configService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OllamaServiceImpl(LLMConfigService configService) {
        this.configService = configService;
    }

    @Override
    public LLMProvider getProvider() {
        return LLMProvider.OLLAMA;
    }

    @Override
    public Flux<String> streamChat(String systemPrompt, String userPrompt) {
        LLMConfigEntity config = configService.get();
        String baseUrl = config.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:11434";
        }

        String combinedPrompt = systemPrompt + "\n\n" + userPrompt;

        Map<String, Object> body = Map.of(
            "model", config.getModel(),
            "prompt", combinedPrompt,
            "stream", true
        );

        WebClient client = WebClient.builder().baseUrl(baseUrl).build();

        return client.post()
                .uri("/api/generate")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> !line.isBlank())
                .map(line -> {
                    try {
                        JsonNode node = objectMapper.readTree(line);
                        JsonNode response = node.get("response");
                        return response != null ? response.asText() : "";
                    } catch (Exception e) {
                        return "";
                    }
                });
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/classnote/llm/OllamaServiceImpl.java
git commit -m "feat: add Ollama LLM service impl"
```

---

### Task 8: Analysis DTOs and AnalysisService

**Files:**
- Create: `src/main/java/com/classnote/dto/FileItem.java`
- Create: `src/main/java/com/classnote/dto/AnalysisRequest.java`
- Create: `src/main/java/com/classnote/dto/AnalysisEvent.java`
- Create: `src/main/java/com/classnote/service/AnalysisService.java`

- [ ] **Step 1: Create FileItem DTO**

```java
package com.classnote.dto;

public class FileItem {
    private String path;
    private String content;

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
```

- [ ] **Step 2: Create AnalysisRequest DTO**

```java
package com.classnote.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

public class AnalysisRequest {
    @NotEmpty
    @Size(max = 100)
    private List<FileItem> files;
    private String projectName;

    public List<FileItem> getFiles() { return files; }
    public void setFiles(List<FileItem> files) { this.files = files; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
}
```

- [ ] **Step 3: Create AnalysisEvent DTO**

```java
package com.classnote.dto;

public class AnalysisEvent {
    private String phase;     // PER_FILE, PACKAGE, OVERVIEW, ERROR, DONE
    private String path;      // file path or package name
    private String content;   // streaming chunk
    private boolean complete; // whether this phase is done

    public AnalysisEvent() {}

    public AnalysisEvent(String phase, String path, String content, boolean complete) {
        this.phase = phase;
        this.path = path;
        this.content = content;
        this.complete = complete;
    }

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isComplete() { return complete; }
    public void setComplete(boolean complete) { this.complete = complete; }
}
```

- [ ] **Step 4: Create AnalysisService**

```java
package com.classnote.service;

import com.classnote.dto.AnalysisEvent;
import com.classnote.dto.FileItem;
import com.classnote.llm.LLMService;
import com.classnote.llm.LLMServiceFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private final LLMServiceFactory llmFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        // Group files by package/directory
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
        ).concatWith(Flux.just(new AnalysisEvent("DONE", null, "", true)))
        .timeout(Duration.ofSeconds(120))
        .retry(2);
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
            }, 5); // concurrency 5
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
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/classnote/dto/FileItem.java src/main/java/com/classnote/dto/AnalysisRequest.java src/main/java/com/classnote/dto/AnalysisEvent.java src/main/java/com/classnote/service/AnalysisService.java
git commit -m "feat: add analysis service with three-phase prompt strategy"
```

---

### Task 9: Analysis SSE controller

**Files:**
- Create: `src/main/java/com/classnote/controller/AnalysisController.java`

- [ ] **Step 1: Create AnalysisController with SSE endpoint**

```java
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
        SseEmitter emitter = new SseEmitter(300000L); // 5 min timeout

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
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/classnote/controller/AnalysisController.java
git commit -m "feat: add analysis SSE controller"
```

---

### Task 10: Knowledge base entity, repository, and service

**Files:**
- Create: `src/main/java/com/classnote/entity/KnowledgeEntry.java`
- Create: `src/main/java/com/classnote/repository/KnowledgeEntryRepository.java`
- Create: `src/main/java/com/classnote/service/KnowledgeService.java`
- Create: `src/main/java/com/classnote/dto/KnowledgeEntryRequest.java`
- Create: `src/main/java/com/classnote/dto/KnowledgeEntryResponse.java`

- [ ] **Step 1: Create KnowledgeEntry entity**

```java
package com.classnote.entity;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_entries")
public class KnowledgeEntry {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false, length = 100)
    private String projectName;

    @Column(nullable = false, length = 20)
    private String category;

    @Column(length = 500)
    private String filePath;

    @Column(length = 200)
    private String packageName;

    @Column(length = 36)
    private String sourceNoteId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getSourceNoteId() { return sourceNoteId; }
    public void setSourceNoteId(String sourceNoteId) { this.sourceNoteId = sourceNoteId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

- [ ] **Step 2: Create KnowledgeEntryRepository**

```java
package com.classnote.repository;

import com.classnote.entity.KnowledgeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeEntryRepository extends JpaRepository<KnowledgeEntry, String> {
    List<KnowledgeEntry> findByProjectNameOrderByCreatedAtDesc(String projectName);
}
```

- [ ] **Step 3: Create KnowledgeEntryRequest DTO**

```java
package com.classnote.dto;

import javax.validation.constraints.NotBlank;

public class KnowledgeEntryRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    @NotBlank
    private String projectName;
    @NotBlank
    private String category;
    private String filePath;
    private String packageName;
    private String sourceNoteId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getSourceNoteId() { return sourceNoteId; }
    public void setSourceNoteId(String sourceNoteId) { this.sourceNoteId = sourceNoteId; }
}
```

- [ ] **Step 4: Create KnowledgeEntryResponse DTO**

```java
package com.classnote.dto;

import com.classnote.entity.KnowledgeEntry;
import java.time.LocalDateTime;

public class KnowledgeEntryResponse {
    private String id;
    private String title;
    private String content;
    private String projectName;
    private String category;
    private String filePath;
    private String packageName;
    private String sourceNoteId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KnowledgeEntryResponse from(KnowledgeEntry entry) {
        KnowledgeEntryResponse r = new KnowledgeEntryResponse();
        r.id = entry.getId();
        r.title = entry.getTitle();
        r.content = entry.getContent();
        r.projectName = entry.getProjectName();
        r.category = entry.getCategory();
        r.filePath = entry.getFilePath();
        r.packageName = entry.getPackageName();
        r.sourceNoteId = entry.getSourceNoteId();
        r.createdAt = entry.getCreatedAt();
        r.updatedAt = entry.getUpdatedAt();
        return r;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getSourceNoteId() { return sourceNoteId; }
    public void setSourceNoteId(String sourceNoteId) { this.sourceNoteId = sourceNoteId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

- [ ] **Step 5: Create KnowledgeService**

```java
package com.classnote.service;

import com.classnote.dto.KnowledgeEntryRequest;
import com.classnote.entity.KnowledgeEntry;
import com.classnote.exception.ResourceNotFoundException;
import com.classnote.repository.KnowledgeEntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeService {

    private final KnowledgeEntryRepository repo;

    public KnowledgeService(KnowledgeEntryRepository repo) {
        this.repo = repo;
    }

    public KnowledgeEntry create(KnowledgeEntryRequest req) {
        KnowledgeEntry entry = new KnowledgeEntry();
        entry.setTitle(req.getTitle());
        entry.setContent(req.getContent());
        entry.setProjectName(req.getProjectName());
        entry.setCategory(req.getCategory());
        entry.setFilePath(req.getFilePath());
        entry.setPackageName(req.getPackageName());
        entry.setSourceNoteId(req.getSourceNoteId());
        return repo.save(entry);
    }

    public List<KnowledgeEntry> listByProject(String projectName) {
        return repo.findByProjectNameOrderByCreatedAtDesc(projectName);
    }

    public KnowledgeEntry getById(String id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Knowledge entry not found: " + id));
    }

    public void delete(String id) {
        KnowledgeEntry entry = getById(id);
        repo.delete(entry);
    }
}
```

- [ ] **Step 6: Create KnowledgeController**

```java
package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.KnowledgeEntryRequest;
import com.classnote.dto.KnowledgeEntryResponse;
import com.classnote.service.KnowledgeService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @PostMapping
    public ApiResponse<KnowledgeEntryResponse> create(@Valid @RequestBody KnowledgeEntryRequest req) {
        return ApiResponse.success("知识库条目已创建",
            KnowledgeEntryResponse.from(knowledgeService.create(req)));
    }

    @GetMapping
    public ApiResponse<List<KnowledgeEntryResponse>> list(@RequestParam String project) {
        return ApiResponse.success(
            knowledgeService.listByProject(project).stream()
                .map(KnowledgeEntryResponse::from)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeEntryResponse> get(@PathVariable String id) {
        return ApiResponse.success(
            KnowledgeEntryResponse.from(knowledgeService.getById(id)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        knowledgeService.delete(id);
        return ApiResponse.success("知识库条目已删除");
    }
}
```

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/classnote/entity/KnowledgeEntry.java src/main/java/com/classnote/repository/KnowledgeEntryRepository.java src/main/java/com/classnote/service/KnowledgeService.java src/main/java/com/classnote/dto/KnowledgeEntryRequest.java src/main/java/com/classnote/dto/KnowledgeEntryResponse.java src/main/java/com/classnote/controller/KnowledgeController.java
git commit -m "feat: add knowledge base entity, service, and REST endpoints"
```

---

### Task 11: Frontend — analysis API and store

**Files:**
- Create: `frontend/src/api/analysis.js`
- Create: `frontend/src/api/knowledge.js`
- Modify: `frontend/src/api/llm.js` (new)
- Create: `frontend/src/stores/analysis.js`
- Create: `frontend/src/stores/knowledge.js`

- [ ] **Step 1: Create LLM config API**

```javascript
// frontend/src/api/llm.js
import api from './index'

export const llmApi = {
  getConfig: () => api.get('/llm/config'),
  updateConfig: (data) => api.put('/llm/config', data)
}
```

- [ ] **Step 2: Create analysis API with SSE streaming**

```javascript
// frontend/src/api/analysis.js

export function streamAnalysis(files, projectName) {
  const controller = new AbortController()

  const promise = fetch('/api/v1/analysis/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ files, projectName }),
    signal: controller.signal
  })

  return {
    controller,
    response: promise
  }
}
```

- [ ] **Step 3: Create knowledge base API**

```javascript
// frontend/src/api/knowledge.js
import api from './index'

export const knowledgeApi = {
  create: (data) => api.post('/knowledge', data),
  listByProject: (project) => api.get('/knowledge', { params: { project } }),
  get: (id) => api.get(`/knowledge/${id}`),
  delete: (id) => api.delete(`/knowledge/${id}`)
}
```

- [ ] **Step 4: Create analysis Pinia store**

```javascript
// frontend/src/stores/analysis.js
import { defineStore } from 'pinia'

export const useAnalysisStore = defineStore('analysis', {
  state: () => ({
    phase: 'idle',        // idle | uploading | analyzing | done | error
    currentStage: '',     // PER_FILE | PACKAGE | OVERVIEW
    currentTarget: '',    // current file/package being analyzed
    streamContent: '',    // accumulated markdown
    error: null,
    fileTree: [],         // [{ path, content, selected }]
    projectName: ''
  }),
  actions: {
    reset() {
      this.phase = 'idle'
      this.currentStage = ''
      this.currentTarget = ''
      this.streamContent = ''
      this.error = null
      this.fileTree = []
      this.projectName = ''
    },
    resetAnalysis() {
      this.phase = 'analyzing'
      this.currentStage = ''
      this.currentTarget = ''
      this.streamContent = ''
      this.error = null
    },
    setFileTree(tree) {
      this.fileTree = tree
    },
    toggleFile(path) {
      const file = this.fileTree.find(f => f.path === path)
      if (file) file.selected = !file.selected
    },
    selectAll() {
      this.fileTree.forEach(f => { f.selected = true })
    },
    deselectAll() {
      this.fileTree.forEach(f => { f.selected = false })
    },
    getSelectedFiles() {
      return this.fileTree.filter(f => f.selected).map(f => ({ path: f.path, content: f.content }))
    },
    handleEvent(event) {
      if (event.phase === 'ERROR') {
        this.phase = 'error'
        this.error = event.content
        return
      }
      if (event.phase === 'DONE') {
        this.phase = 'done'
        return
      }
      if (this.phase !== 'analyzing') {
        this.phase = 'analyzing'
      }
      this.currentStage = event.phase
      this.currentTarget = event.path || ''
      this.streamContent += event.content
    },
    setError(msg) {
      this.phase = 'error'
      this.error = msg
    }
  }
})
```

- [ ] **Step 5: Commit**

```bash
git add frontend/src/api/analysis.js frontend/src/api/knowledge.js frontend/src/api/llm.js frontend/src/stores/analysis.js
git commit -m "feat: add frontend analysis API, store, and knowledge API"
```

---

### Task 12: Frontend — FolderDropZone component

**Files:**
- Create: `frontend/src/components/analysis/FolderDropZone.vue`

- [ ] **Step 1: Create FolderDropZone.vue**

```vue
<template>
  <div
    class="drop-zone"
    :class="{ 'drop-zone--active': dragging }"
    @dragover.prevent="dragging = true"
    @dragleave.prevent="dragging = false"
    @drop.prevent="handleDrop"
  >
    <el-icon :size="48"><UploadFilled /></el-icon>
    <p>拖拽项目文件夹到此处</p>
    <p class="drop-zone__hint">支持 Java、Python、JavaScript 等项目源码文件夹</p>
  </div>
</template>

<script>
import { ref } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'

export default {
  name: 'FolderDropZone',
  components: { UploadFilled },
  emits: ['filesLoaded'],
  setup(props, { emit }) {
    const dragging = ref(false)

    async function readEntry(entry) {
      if (entry.isFile) {
        return new Promise((resolve) => {
          entry.file((file) => {
            const reader = new FileReader()
            reader.onload = () => resolve({
              path: file.webkitRelativePath || entry.fullPath.substring(1),
              content: reader.result,
              selected: true
            })
            reader.onerror = () => resolve(null)
            reader.readAsText(file)
          })
        })
      }
      if (entry.isDirectory) {
        const reader = entry.createReader()
        const entries = await new Promise((resolve) => {
          reader.readEntries(resolve)
        })
        const results = await Promise.all(
          entries.map(e => readEntry(e))
        )
        return results.flat().filter(Boolean)
      }
      return []
    }

    async function handleDrop(e) {
      dragging.value = false
      const items = [...e.dataTransfer.items]
      const results = []
      for (const item of items) {
        const entry = item.webkitGetAsEntry()
        if (entry) {
          const files = await readEntry(entry)
          results.push(...files)
        }
      }
      if (results.length > 0) {
        const projectName = results[0].path.split('/')[0] || 'Unknown Project'
        emit('filesLoaded', { files: results, projectName })
      }
    }

    return { dragging, handleDrop }
  }
}
</script>

<style scoped>
.drop-zone {
  border: 2px dashed var(--border-color);
  border-radius: 12px;
  padding: 60px 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  color: var(--text-secondary);
}
.drop-zone--active {
  border-color: var(--brand-color);
  background: rgba(var(--brand-color-rgb), 0.05);
  color: var(--brand-color);
}
.drop-zone__hint {
  font-size: 12px;
  margin-top: 8px;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/analysis/FolderDropZone.vue
git commit -m "feat: add folder drag-and-drop zone component"
```

---

### Task 13: Frontend — FileTreeSelector component

**Files:**
- Create: `frontend/src/components/analysis/FileTreeSelector.vue`

- [ ] **Step 1: Create FileTreeSelector.vue**

```vue
<template>
  <div v-if="files.length > 0" class="file-tree">
    <div class="file-tree__header">
      <span>{{ files.length }} 个文件</span>
      <div>
        <el-button size="small" text @click="$emit('selectAll')">全选</el-button>
        <el-button size="small" text @click="$emit('deselectAll')">取消全选</el-button>
      </div>
    </div>
    <el-tree
      :data="treeData"
      :props="{ children: 'children', label: 'label' }"
      node-key="key"
      show-checkbox
      :default-expand-all="files.length < 50"
      :default-checked-keys="checkedKeys"
      @check-change="onCheckChange"
    >
      <template #default="{ data }">
        <span class="file-tree__node">
          <el-icon v-if="data.isDir"><Folder /></el-icon>
          <el-icon v-else><Document /></el-icon>
          <span>{{ data.label }}</span>
        </span>
      </template>
    </el-tree>
  </div>
</template>

<script>
import { computed } from 'vue'
import { Folder, Document } from '@element-plus/icons-vue'

export default {
  name: 'FileTreeSelector',
  components: { Folder, Document },
  props: {
    files: { type: Array, default: () => [] }
  },
  emits: ['toggleFile'],
  setup(props, { emit }) {
    function buildTree(fileList) {
      const root = {}
      for (const f of fileList) {
        const parts = f.path.replace(/\\/g, '/').split('/')
        let current = root
        for (let i = 0; i < parts.length; i++) {
          const part = parts[i]
          if (i === parts.length - 1) {
            if (!current._files) current._files = []
            current._files.push({ label: part, key: f.path, isDir: false })
          } else {
            if (!current[part]) current[part] = {}
            current = current[part]
          }
        }
      }
      return convertNode(root, '')
    }

    function convertNode(node, prefix) {
      const result = []
      // dirs
      for (const [name, child] of Object.entries(node)) {
        if (name.startsWith('_')) continue
        const fullPath = prefix ? prefix + '/' + name : name
        result.push({
          label: name,
          key: fullPath,
          isDir: true,
          children: convertNode(child, fullPath)
        })
      }
      // files
      if (node._files) {
        result.push(...node._files)
      }
      return result
    }

    const treeData = computed(() => buildTree(props.files))
    const checkedKeys = computed(() =>
      props.files.filter(f => f.selected).map(f => f.path)
    )

    function onCheckChange(data, checked) {
      if (!data.isDir) {
        emit('toggleFile', data.key)
      }
    }

    return { treeData, checkedKeys, onCheckChange }
  }
}
</script>

<style scoped>
.file-tree {
  margin-top: 16px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 12px;
  max-height: 400px;
  overflow-y: auto;
}
.file-tree__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
  font-size: 13px;
  color: var(--text-secondary);
}
.file-tree__node {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/analysis/FileTreeSelector.vue
git commit -m "feat: add file tree selector component"
```

---

### Task 14: Frontend — AnalysisProgress and AnalysisResult components

**Files:**
- Create: `frontend/src/components/analysis/AnalysisProgress.vue`
- Create: `frontend/src/components/analysis/AnalysisResult.vue`

- [ ] **Step 1: Create AnalysisProgress.vue**

```vue
<template>
  <div v-if="phase === 'analyzing'" class="analysis-progress">
    <el-progress :percentage="progressPercent" :stroke-width="6" />
    <div class="analysis-progress__info">
      <span class="analysis-progress__stage">{{ stageLabel }}</span>
      <span class="analysis-progress__target">{{ currentTarget }}</span>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'AnalysisProgress',
  props: {
    phase: String,
    currentStage: String,
    currentTarget: String
  },
  setup(props) {
    const stageLabel = computed(() => {
      switch (props.currentStage) {
        case 'PER_FILE': return '正在分析文件…'
        case 'PACKAGE': return '正在汇总模块…'
        case 'OVERVIEW': return '正在生成项目总览…'
        default: return ''
      }
    })
    const progressPercent = computed(() => {
      switch (props.currentStage) {
        case 'PER_FILE': return 30
        case 'PACKAGE': return 60
        case 'OVERVIEW': return 85
        default: return 0
      }
    })
    return { stageLabel, progressPercent }
  }
}
</script>

<style scoped>
.analysis-progress {
  margin: 20px 0;
  padding: 16px;
  background: var(--bg-secondary);
  border-radius: 8px;
}
.analysis-progress__info {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
```

- [ ] **Step 2: Create AnalysisResult.vue**

```vue
<template>
  <div v-if="content" class="analysis-result">
    <div class="analysis-result__content" v-html="renderedMarkdown"></div>
  </div>
</template>

<script>
import { computed } from 'vue'
import { marked } from 'marked'

export default {
  name: 'AnalysisResult',
  props: {
    content: { type: String, default: '' }
  },
  setup(props) {
    const renderedMarkdown = computed(() => {
      if (!props.content) return ''
      return marked.parse(props.content)
    })
    return { renderedMarkdown }
  }
}
</script>

<style scoped>
.analysis-result {
  margin-top: 20px;
}
.analysis-result__content {
  padding: 20px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  line-height: 1.7;
}
.analysis-result__content :deep(h2) {
  margin-top: 24px;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 8px;
}
.analysis-result__content :deep(code) {
  background: var(--bg-secondary);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}
.analysis-result__content :deep(pre) {
  background: var(--bg-secondary);
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
}
</style>
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/analysis/AnalysisProgress.vue frontend/src/components/analysis/AnalysisResult.vue
git commit -m "feat: add analysis progress and result components"
```

---

### Task 15: Frontend — SaveResultDialog component

**Files:**
- Create: `frontend/src/components/analysis/SaveResultDialog.vue`

- [ ] **Step 1: Create SaveResultDialog.vue**

```vue
<template>
  <el-dialog v-model="visible" title="保存分析结果" width="500px" @close="handleClose">
    <el-form :model="form" label-width="80px">
      <el-form-item label="保存方式">
        <el-radio-group v-model="form.saveType">
          <el-radio value="note">存为笔记</el-radio>
          <el-radio value="knowledge">存为知识库</el-radio>
          <el-radio value="both">两者都存</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="输入标题" />
      </el-form-item>
      <el-form-item v-if="form.saveType !== 'knowledge'" label="笔记本">
        <el-select v-model="form.notebookId" placeholder="选择笔记本" style="width:100%">
          <el-option v-for="nb in notebooks" :key="nb.id" :label="nb.name" :value="nb.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="项目名">
        <el-input v-model="form.projectName" placeholder="项目名称" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
    </template>
  </el-dialog>
</template>

<script>
import { ref, watch } from 'vue'
import { notebookApi } from '../../api/notebooks'
import { useNotebookStore } from '../../stores/notebook'

export default {
  name: 'SaveResultDialog',
  props: {
    modelValue: Boolean,
    content: String,
    projectName: String
  },
  emits: ['update:modelValue', 'save'],
  setup(props, { emit }) {
    const visible = ref(false)
    const saving = ref(false)
    const notebooks = ref([])
    const form = ref({
      saveType: 'note',
      title: '',
      notebookId: '',
      projectName: ''
    })

    watch(() => props.modelValue, (val) => {
      visible.value = val
      if (val) {
        form.value.title = (props.projectName || 'Project') + ' 分析报告'
        form.value.projectName = props.projectName || ''
        loadNotebooks()
      }
    })

    async function loadNotebooks() {
      const res = await notebookApi.list({ size: 50 })
      notebooks.value = res.data.content || []
      const nbStore = useNotebookStore()
      if (nbStore.currentNotebookId) {
        form.value.notebookId = nbStore.currentNotebookId
      }
    }

    function handleClose() {
      emit('update:modelValue', false)
    }

    function handleSave() {
      emit('save', {
        ...form.value,
        content: props.content
      })
    }

    return { visible, saving, notebooks, form, handleClose, handleSave }
  }
}
</script>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/components/analysis/SaveResultDialog.vue
git commit -m "feat: add save result dialog component"
```

---

### Task 16: Frontend — ProjectAnalyzer view

**Files:**
- Create: `frontend/src/views/ProjectAnalyzer.vue`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/components/Sidebar.vue`

- [ ] **Step 1: Create ProjectAnalyzer.vue**

```vue
<template>
  <div class="project-analyzer">
    <div class="project-analyzer__header">
      <h3>项目分析</h3>
      <el-button v-if="store.phase === 'done'" type="primary" @click="showSaveDialog = true">
        保存结果
      </el-button>
    </div>

    <FolderDropZone
      v-if="store.phase === 'idle'"
      @filesLoaded="onFilesLoaded"
    />

    <FileTreeSelector
      v-if="store.phase === 'idle' && store.fileTree.length > 0"
      :files="store.fileTree"
      @toggleFile="store.toggleFile"
      @selectAll="store.selectAll"
      @deselectAll="store.deselectAll"
    />

    <div v-if="store.phase === 'idle' && store.fileTree.length > 0" class="project-analyzer__actions">
      <el-button type="primary" :disabled="store.getSelectedFiles().length === 0"
        @click="startAnalysis">
        开始分析 ({{ store.getSelectedFiles().length }} 个文件)
      </el-button>
      <el-button @click="store.reset()">清除</el-button>
    </div>

    <AnalysisProgress
      :phase="store.phase"
      :currentStage="store.currentStage"
      :currentTarget="store.currentTarget"
    />

    <div v-if="store.phase === 'error'" class="project-analyzer__error">
      <el-alert :title="store.error" type="error" show-icon :closable="false" />
      <el-button style="margin-top:12px" @click="store.reset()">重试</el-button>
    </div>

    <AnalysisResult :content="store.streamContent" />

    <SaveResultDialog
      v-model="showSaveDialog"
      :content="store.streamContent"
      :projectName="store.projectName"
      @save="handleSave"
    />
  </div>
</template>

<script>
import { ref } from 'vue'
import { useAnalysisStore } from '../stores/analysis'
import { useNoteStore } from '../stores/note'
import { useNotebookStore } from '../stores/notebook'
import { streamAnalysis } from '../api/analysis'
import { knowledgeApi } from '../api/knowledge'
import { noteApi } from '../api/notes'
import FolderDropZone from '../components/analysis/FolderDropZone.vue'
import FileTreeSelector from '../components/analysis/FileTreeSelector.vue'
import AnalysisProgress from '../components/analysis/AnalysisProgress.vue'
import AnalysisResult from '../components/analysis/AnalysisResult.vue'
import SaveResultDialog from '../components/analysis/SaveResultDialog.vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'ProjectAnalyzer',
  components: { FolderDropZone, FileTreeSelector, AnalysisProgress, AnalysisResult, SaveResultDialog },
  setup() {
    const store = useAnalysisStore()
    const noteStore = useNoteStore()
    const nbStore = useNotebookStore()
    const showSaveDialog = ref(false)
    let abortController = null

    function onFilesLoaded({ files, projectName }) {
      store.setFileTree(files)
      store.projectName = projectName
    }

    async function startAnalysis() {
      const selectedFiles = store.getSelectedFiles()
      if (selectedFiles.length === 0) return
      store.resetAnalysis()

      const { response, controller } = streamAnalysis(selectedFiles, store.projectName)
      abortController = controller

      try {
        const reader = response.then(r => {
          if (!r.ok) throw new Error('Analysis request failed: ' + r.statusText)
          return r.body.getReader()
        })
        const streamReader = await reader
        const decoder = new TextDecoder()
        let buffer = ''

        while (true) {
          const { done, value } = await streamReader.read()
          if (done) break

          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop() || ''

          for (const line of lines) {
            if (line.startsWith('event:analysis')) continue
            if (line.startsWith('data:')) {
              try {
                const event = JSON.parse(line.substring(5).trim())
                store.handleEvent(event)
              } catch (e) {
                // skip parse errors
              }
            }
          }
        }
      } catch (e) {
        if (e.name !== 'AbortError') {
          store.setError(e.message || 'Analysis failed')
        }
      }
    }

    async function handleSave({ saveType, title, notebookId, projectName, content }) {
      try {
        if (saveType === 'note' || saveType === 'both') {
          await noteApi.create({ title, content, notebookId })
          ElMessage.success('已保存为笔记')
        }
        if (saveType === 'knowledge' || saveType === 'both') {
          await knowledgeApi.create({
            title,
            content,
            projectName,
            category: 'OVERVIEW'
          })
          ElMessage.success('已保存到知识库')
        }
        showSaveDialog.value = false
      } catch (e) {
        ElMessage.error('保存失败: ' + e.message)
      }
    }

    store.reset()
    return { store, showSaveDialog, onFilesLoaded, startAnalysis, handleSave }
  }
}
</script>

<style scoped>
.project-analyzer {
  padding: 24px;
  max-width: 960px;
  margin: 0 auto;
  height: 100%;
  overflow-y: auto;
}
.project-analyzer__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.project-analyzer__header h3 {
  margin: 0;
  font-size: 18px;
}
.project-analyzer__actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}
.project-analyzer__error {
  margin-top: 20px;
}
</style>
```

- [ ] **Step 2: Add route to router/index.js**

Insert after the search route:

```javascript
{
  path: 'analyzer',
  name: 'ProjectAnalyzer',
  component: () => import('../views/ProjectAnalyzer.vue')
},
{
  path: 'knowledge',
  name: 'KnowledgeBase',
  component: () => import('../views/KnowledgeBase.vue')
}
```

- [ ] **Step 3: Add navigation items to Sidebar.vue**

Insert before the `</el-menu>` closing tag (before the manage tags item):

```html
<el-menu-item index="/analyzer">
  <el-icon><DataAnalysis /></el-icon>
  <span>项目分析</span>
</el-menu-item>

<el-menu-item index="/knowledge">
  <el-icon><Collection /></el-icon>
  <span>知识库</span>
</el-menu-item>
```

Also add the icons to the Sidebar import:

```javascript
import { DataAnalysis, Collection } from '@element-plus/icons-vue'
```

And update `activeMenu` computed to include new routes:

```javascript
if (route.path.startsWith('/analyzer')) return '/analyzer'
if (route.path.startsWith('/knowledge')) return '/knowledge'
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/views/ProjectAnalyzer.vue frontend/src/router/index.js frontend/src/components/Sidebar.vue
git commit -m "feat: add project analyzer view, routes, and sidebar nav"
```

---

### Task 17: Frontend — KnowledgeBase view and settings page LLM card

**Files:**
- Create: `frontend/src/views/KnowledgeBase.vue`
- Modify: `frontend/src/stores/preference.js` (or create standalone settings)

- [ ] **Step 1: Create KnowledgeBase.vue**

```vue
<template>
  <div class="knowledge-base">
    <h3>知识库</h3>
    <div v-if="entries.length === 0" class="knowledge-base__empty">
      <p>暂无知识库条目</p>
      <p class="knowledge-base__hint">通过「项目分析」生成的知识点可以保存到此处</p>
    </div>
    <div v-else>
      <el-input v-model="searchProject" placeholder="按项目名搜索…" size="small" clearable
        style="width:300px; margin-bottom:16px;" @input="loadEntries" />
      <el-card v-for="entry in entries" :key="entry.id" class="knowledge-card" shadow="hover">
        <template #header>
          <div class="knowledge-card__header">
            <span>{{ entry.title }}</span>
            <el-tag size="small" :type="categoryType(entry.category)">
              {{ entry.category }}
            </el-tag>
          </div>
        </template>
        <div class="knowledge-card__meta">
          <span>项目: {{ entry.projectName }}</span>
          <span v-if="entry.packageName">包: {{ entry.packageName }}</span>
          <span>{{ entry.createdAt }}</span>
        </div>
        <div v-html="renderedPreview(entry.content)" class="knowledge-card__preview"></div>
        <div class="knowledge-card__actions">
          <el-button size="small" text @click="viewDetail(entry)">查看详情</el-button>
          <el-button size="small" text type="danger" @click="deleteEntry(entry.id)">删除</el-button>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="detailVisible" :title="detailEntry?.title" width="700px">
      <div v-if="detailEntry" v-html="renderedFull(detailEntry.content)"></div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { knowledgeApi } from '../api/knowledge'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'

export default {
  name: 'KnowledgeBase',
  setup() {
    const entries = ref([])
    const searchProject = ref('')
    const detailVisible = ref(false)
    const detailEntry = ref(null)

    async function loadEntries() {
      if (!searchProject.value) {
        entries.value = []
        return
      }
      try {
        const res = await knowledgeApi.listByProject(searchProject.value)
        entries.value = res.data || []
      } catch (e) {
        ElMessage.error('加载失败')
      }
    }

    function categoryType(cat) {
      return cat === 'OVERVIEW' ? 'primary' : cat === 'PACKAGE' ? 'success' : 'info'
    }

    function renderedPreview(content) {
      if (!content) return ''
      return marked.parse(content.substring(0, 300) + '...')
    }

    function renderedFull(content) {
      return content ? marked.parse(content) : ''
    }

    function viewDetail(entry) {
      detailEntry.value = entry
      detailVisible.value = true
    }

    async function deleteEntry(id) {
      try {
        await ElMessageBox.confirm('确认删除该知识库条目？', '确认')
        await knowledgeApi.delete(id)
        entries.value = entries.value.filter(e => e.id !== id)
        ElMessage.success('已删除')
      } catch (e) {
        // cancelled
      }
    }

    return { entries, searchProject, detailVisible, detailEntry,
      loadEntries, categoryType, renderedPreview, renderedFull, viewDetail, deleteEntry }
  }
}
</script>

<style scoped>
.knowledge-base {
  padding: 24px;
  max-width: 900px;
  margin: 0 auto;
  height: 100%;
  overflow-y: auto;
}
.knowledge-base h3 { margin: 0 0 20px; font-size: 18px; }
.knowledge-base__empty { text-align: center; padding: 60px 0; color: var(--text-secondary); }
.knowledge-base__hint { font-size: 13px; margin-top: 8px; }
.knowledge-card { margin-bottom: 12px; }
.knowledge-card__header { display: flex; justify-content: space-between; align-items: center; }
.knowledge-card__meta {
  display: flex; gap: 16px; font-size: 12px; color: var(--text-secondary); margin-bottom: 8px;
}
.knowledge-card__preview { font-size: 13px; line-height: 1.6; max-height: 80px; overflow: hidden; }
.knowledge-card__actions { margin-top: 10px; display: flex; gap: 8px; }
</style>
```

- [ ] **Step 2: Create LLM settings component and integrate**

Create `frontend/src/components/LLMSettings.vue`:

```vue
<template>
  <el-card class="llm-settings">
    <template #header><span>大模型配置</span></template>
    <el-form :model="form" label-width="100px">
      <el-form-item label="提供商">
        <el-select v-model="form.provider" style="width:100%">
          <el-option label="OpenAI" value="OPENAI" />
          <el-option label="Anthropic" value="ANTHROPIC" />
          <el-option label="Ollama (本地)" value="OLLAMA" />
          <el-option label="DeepSeek" value="DEEPSEEK" />
          <el-option label="通义千问" value="QWEN" />
          <el-option label="自定义兼容" value="CUSTOM" />
        </el-select>
      </el-form-item>
      <el-form-item label="API Key">
        <el-input v-model="form.apiKey" type="password" show-password placeholder="sk-..." />
      </el-form-item>
      <el-form-item label="Base URL">
        <el-input v-model="form.baseUrl" placeholder="可选，留空使用默认" />
      </el-form-item>
      <el-form-item label="模型">
        <el-input v-model="form.model" placeholder="gpt-4o / claude-sonnet-4-6 / llama3" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="save">保存配置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script>
import { ref, onMounted } from 'vue'
import { llmApi } from '../api/llm'
import { ElMessage } from 'element-plus'

export default {
  name: 'LLMSettings',
  setup() {
    const form = ref({ provider: 'OPENAI', apiKey: '', baseUrl: '', model: 'gpt-4o' })

    onMounted(async () => {
      try {
        const res = await llmApi.getConfig()
        const data = res.data
        form.value.provider = data.provider
        form.value.baseUrl = data.baseUrl || ''
        form.value.model = data.model
        // apiKey is masked, don't overwrite unless user types
      } catch (e) {
        // use defaults
      }
    })

    async function save() {
      try {
        await llmApi.updateConfig({
          provider: form.value.provider,
          apiKey: form.value.apiKey || undefined,
          baseUrl: form.value.baseUrl || undefined,
          model: form.value.model
        })
        ElMessage.success('配置已保存')
      } catch (e) {
        ElMessage.error('保存失败')
      }
    }

    return { form, save }
  }
}
</script>
```

This component can be placed in a settings page or accessed via a gear icon. For now, place it at the bottom of `ProjectAnalyzer.vue` behind a collapsible section, or add a `/settings` route.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/views/KnowledgeBase.vue frontend/src/components/LLMSettings.vue
git commit -m "feat: add knowledge base view and LLM settings component"
```

---

### Task 18: Integration testing — verify end-to-end flow

- [ ] **Step 1: Start backend and verify new endpoints**

```bash
cd F:/ClassingNote && mvn spring-boot:run
```

Test:
```bash
# Check LLM config
curl http://localhost:8080/api/v1/llm/config

# Check knowledge base is empty
curl "http://localhost:8080/api/v1/knowledge?project=test"
```

Expected: `GET /api/v1/llm/config` returns default config with masked key. Knowledge returns empty list.

- [ ] **Step 2: Start frontend dev server**

```bash
cd F:/ClassingNote/frontend && npm run dev
```

Open `http://localhost:5173/analyzer` and verify:
1. Drop zone renders
2. Dragging a folder populates file tree
3. "Start Analysis" button appears
4. Sidebar shows "项目分析" and "知识库" nav items

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "chore: verify end-to-end integration"
```
