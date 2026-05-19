# ClassNote Local Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Build a complete local-first classroom note application with Spring Boot backend (25 REST APIs) and Vue 3 + Element Plus frontend.

**Architecture:** Spring Boot 2.7+ with JPA/MySQL backend serving REST APIs and static Vue 3 frontend. Single-module Maven project. Frontend built with Vite, packaged into Spring Boot's static resources. All data stored locally in MySQL 8.0, files on local filesystem.

**Tech Stack:** Spring Boot 2.7, JPA/Hibernate, MySQL 8.0, Vue 3, Element Plus, Vite, Axios, Vue Router, Pinia, Marked (Markdown rendering), CodeMirror (editor)

---

## File Structure

```
ClassingNote/
├── pom.xml
├── src/main/java/com/classnote/
│   ├── ClassNoteApplication.java
│   ├── common/
│   │   └── ApiResponse.java
│   ├── config/
│   │   ├── WebConfig.java
│   │   └── FileStorageConfig.java
│   ├── entity/
│   │   ├── Notebook.java
│   │   ├── Note.java
│   │   ├── Tag.java
│   │   ├── Attachment.java
│   │   ├── Draft.java
│   │   └── UserPreference.java
│   ├── repository/
│   │   ├── NotebookRepository.java
│   │   ├── NoteRepository.java
│   │   ├── TagRepository.java
│   │   ├── AttachmentRepository.java
│   │   ├── DraftRepository.java
│   │   └── UserPreferenceRepository.java
│   ├── dto/
│   │   ├── NotebookRequest.java
│   │   ├── NoteRequest.java
│   │   ├── TagRequest.java
│   │   ├── BatchDeleteRequest.java
│   │   ├── DraftRequest.java
│   │   ├── PreferenceRequest.java
│   │   ├── NotebookResponse.java
│   │   ├── NoteListResponse.java
│   │   ├── NoteDetailResponse.java
│   │   ├── TagResponse.java
│   │   ├── AttachmentResponse.java
│   │   ├── DraftResponse.java
│   │   ├── ExportResponse.java
│   │   └── DeleteResult.java
│   ├── service/
│   │   ├── NotebookService.java
│   │   ├── NoteService.java
│   │   ├── TagService.java
│   │   ├── AttachmentService.java
│   │   ├── DraftService.java
│   │   ├── PreferenceService.java
│   │   ├── ExportService.java
│   │   └── SearchService.java
│   ├── controller/
│   │   ├── NotebookController.java
│   │   ├── NoteController.java
│   │   ├── TagController.java
│   │   ├── AttachmentController.java
│   │   ├── DraftController.java
│   │   ├── PreferenceController.java
│   │   ├── ConfigController.java
│   │   ├── ExportController.java
│   │   └── SearchController.java
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       ├── ResourceNotFoundException.java
│       └── DuplicateResourceException.java
├── src/main/resources/
│   └── application.yml
└── frontend/
    ├── package.json
    ├── vite.config.js
    ├── index.html
    └── src/
        ├── main.js
        ├── App.vue
        ├── api/
        │   ├── index.js
        │   ├── notebooks.js
        │   ├── notes.js
        │   ├── tags.js
        │   ├── search.js
        │   ├── attachments.js
        │   ├── drafts.js
        │   ├── preferences.js
        │   └── export.js
        ├── router/
        │   └── index.js
        ├── stores/
        │   ├── notebook.js
        │   ├── note.js
        │   ├── tag.js
        │   ├── draft.js
        │   └── preference.js
        ├── views/
        │   ├── NotebookView.vue
        │   ├── NoteEditor.vue
        │   ├── TagManager.vue
        │   └── SearchView.vue
        ├── components/
        │   ├── AppLayout.vue
        │   ├── Sidebar.vue
        │   ├── MarkdownEditor.vue
        │   ├── MarkdownPreview.vue
        │   ├── NotebookDialog.vue
        │   ├── TagDialog.vue
        │   └── NoteList.vue
        └── styles/
            └── global.css
```

---

### Task 1: Maven Project Setup & Configuration

**Files:**
- Create: `pom.xml`
- Create: `src/main/resources/application.yml`
- Create: `src/main/java/com/classnote/ClassNoteApplication.java`
- Create: `src/main/java/com/classnote/config/WebConfig.java`
- Create: `src/main/java/com/classnote/config/FileStorageConfig.java`
- Create: `src/main/java/com/classnote/common/ApiResponse.java`

- [ ] **Step 1: Create pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
    </parent>
    <groupId>com.classnote</groupId>
    <artifactId>classnote-local</artifactId>
    <version>1.0.0</version>
    <name>ClassNote Local</name>

    <properties>
        <java.version>11</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Create application.yml**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/classnote?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf-8
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

app:
  version: 1.0.0
  data-directory: ~/.classnote
  attachment-root-path: ~/classnote-files
```

- [ ] **Step 3: Create ClassNoteApplication.java**

```java
package com.classnote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClassNoteApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClassNoteApplication.class, args);
    }
}
```

- [ ] **Step 4: Create ApiResponse.java**

```java
package com.classnote.common;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
```

- [ ] **Step 5: Create WebConfig.java**

```java
package com.classnote.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/attachments/**")
                .addResourceLocations("file:" + System.getProperty("user.home") + "/classnote-files/");
    }
}
```

- [ ] **Step 6: Create FileStorageConfig.java**

```java
package com.classnote.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileStorageConfig {

    @Value("${app.attachment-root-path:~/classnote-files}")
    private String attachmentRootPath;

    private Path resolvedPath;

    @PostConstruct
    public void init() throws IOException {
        String expanded = attachmentRootPath.replaceFirst("^~", System.getProperty("user.home"));
        resolvedPath = Paths.get(expanded).toAbsolutePath();
        Files.createDirectories(resolvedPath);
    }

    public Path getAttachmentRoot() {
        return resolvedPath;
    }
}
```

---

### Task 2: JPA Entities

**Files:**
- Create: `src/main/java/com/classnote/entity/Notebook.java`
- Create: `src/main/java/com/classnote/entity/Note.java`
- Create: `src/main/java/com/classnote/entity/Tag.java`
- Create: `src/main/java/com/classnote/entity/Attachment.java`
- Create: `src/main/java/com/classnote/entity/Draft.java`
- Create: `src/main/java/com/classnote/entity/UserPreference.java`

- [ ] **Step 1: Create Notebook.java**

```java
package com.classnote.entity;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notebooks")
public class Notebook {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "notebook", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("updatedAt DESC")
    private List<Note> notes = new ArrayList<>();

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
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<Note> getNotes() { return notes; }
}
```

- [ ] **Step 2: Create Note.java**

```java
package com.classnote.entity;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String plainText;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notebook_id", nullable = false)
    private Notebook notebook;

    @ManyToMany
    @JoinTable(
        name = "note_tags",
        joinColumns = @JoinColumn(name = "note_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Attachment> attachments = new HashSet<>();

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
    public String getPlainText() { return plainText; }
    public void setPlainText(String plainText) { this.plainText = plainText; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Notebook getNotebook() { return notebook; }
    public void setNotebook(Notebook notebook) { this.notebook = notebook; }
    public Set<Tag> getTags() { return tags; }
    public void setTags(Set<Tag> tags) { this.tags = tags; }
    public Set<Attachment> getAttachments() { return attachments; }
}
```

- [ ] **Step 3: Create Tag.java**

```java
package com.classnote.entity;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @Column(length = 7)
    private String color;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (color == null) color = "#409EFF";
        createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

- [ ] **Step 4: Create Attachment.java**

```java
package com.classnote.entity;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private Long fileSize;

    @Column(length = 100)
    private String mimeType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Note getNote() { return note; }
    public void setNote(Note note) { this.note = note; }
}
```

- [ ] **Step 5: Create Draft.java**

```java
package com.classnote.entity;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "drafts")
public class Draft {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36)
    private String id;

    @Column(length = 36)
    private String noteId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(length = 36)
    private String notebookId;

    @Column(nullable = false)
    private LocalDateTime savedAt;

    @PrePersist
    protected void onCreate() {
        savedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNoteId() { return noteId; }
    public void setNoteId(String noteId) { this.noteId = noteId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getNotebookId() { return notebookId; }
    public void setNotebookId(String notebookId) { this.notebookId = notebookId; }
    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
}
```

- [ ] **Step 6: Create UserPreference.java**

```java
package com.classnote.entity;

import javax.persistence.*;

@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @Column(length = 36)
    private String id = "default";

    @Column(length = 10)
    private String theme = "light";

    @Column(nullable = false)
    private Integer autoSaveInterval = 30;

    @Column(nullable = false)
    private Integer editorFontSize = 14;

    @Column(length = 500)
    private String attachmentRootPath;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public Integer getAutoSaveInterval() { return autoSaveInterval; }
    public void setAutoSaveInterval(Integer autoSaveInterval) { this.autoSaveInterval = autoSaveInterval; }
    public Integer getEditorFontSize() { return editorFontSize; }
    public void setEditorFontSize(Integer editorFontSize) { this.editorFontSize = editorFontSize; }
    public String getAttachmentRootPath() { return attachmentRootPath; }
    public void setAttachmentRootPath(String attachmentRootPath) { this.attachmentRootPath = attachmentRootPath; }
}
```

---

### Task 3: Repositories

**Files:**
- Create: `src/main/java/com/classnote/repository/NotebookRepository.java`
- Create: `src/main/java/com/classnote/repository/NoteRepository.java`
- Create: `src/main/java/com/classnote/repository/TagRepository.java`
- Create: `src/main/java/com/classnote/repository/AttachmentRepository.java`
- Create: `src/main/java/com/classnote/repository/DraftRepository.java`
- Create: `src/main/java/com/classnote/repository/UserPreferenceRepository.java`

- [ ] **Step 1: Create NotebookRepository.java**

```java
package com.classnote.repository;

import com.classnote.entity.Notebook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotebookRepository extends JpaRepository<Notebook, String> {

    @Query("SELECT n FROM Notebook n LEFT JOIN FETCH n.notes ORDER BY n.updatedAt DESC")
    List<Notebook> findAllWithNoteCount();
}
```

- [ ] **Step 2: Create NoteRepository.java**

```java
package com.classnote.repository;

import com.classnote.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, String> {

    Page<Note> findByNotebookId(String notebookId, Pageable pageable);

    @Query("SELECT n FROM Note n LEFT JOIN FETCH n.tags WHERE n.id = :id")
    Note findByIdWithTags(@Param("id") String id);

    @Query("SELECT DISTINCT n FROM Note n LEFT JOIN n.tags t WHERE " +
           "(:notebookId IS NULL OR n.notebook.id = :notebookId) AND " +
           "(:tagId IS NULL OR t.id = :tagId) AND " +
           "(:keyword IS NULL OR n.title LIKE %:keyword% OR n.plainText LIKE %:keyword%)")
    Page<Note> search(@Param("notebookId") String notebookId,
                      @Param("tagId") String tagId,
                      @Param("keyword") String keyword,
                      Pageable pageable);

    @Query("SELECT COUNT(a) > 0 FROM Attachment a WHERE a.filePath = :filePath AND a.note.id <> :noteId")
    boolean existsAttachmentByFilePathExcludingNote(@Param("filePath") String filePath, @Param("noteId") String noteId);

    long countByNotebookId(String notebookId);
}
```

- [ ] **Step 3: Create TagRepository.java**

```java
package com.classnote.repository;

import com.classnote.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {

    boolean existsByName(String name);

    Optional<Tag> findByName(String name);

    @Query("SELECT COUNT(n) FROM Note n JOIN n.tags t WHERE t.id = :tagId")
    long countNotesByTagId(@Param("tagId") String tagId);
}
```

- [ ] **Step 4: Create AttachmentRepository.java**

```java
package com.classnote.repository;

import com.classnote.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, String> {

    List<Attachment> findByNoteId(String noteId);

    @Query("SELECT COUNT(a) > 0 FROM Attachment a WHERE a.filePath = :filePath AND a.note.id <> :noteId")
    boolean existsByFilePathAndNoteIdNot(@Param("filePath") String filePath, @Param("noteId") String noteId);

    void deleteByNoteIdIn(List<String> noteIds);
}
```

- [ ] **Step 5: Create DraftRepository.java**

```java
package com.classnote.repository;

import com.classnote.entity.Draft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DraftRepository extends JpaRepository<Draft, String> {

    Draft findTopByOrderBySavedAtDesc();

    void deleteAll();
}
```

- [ ] **Step 6: Create UserPreferenceRepository.java**

```java
package com.classnote.repository;

import com.classnote.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, String> {
}
```

---

### Task 4: DTOs

**Files:**
- Create: `src/main/java/com/classnote/dto/NotebookRequest.java`
- Create: `src/main/java/com/classnote/dto/NoteRequest.java`
- Create: `src/main/java/com/classnote/dto/TagRequest.java`
- Create: `src/main/java/com/classnote/dto/BatchDeleteRequest.java`
- Create: `src/main/java/com/classnote/dto/DraftRequest.java`
- Create: `src/main/java/com/classnote/dto/PreferenceRequest.java`
- Create: `src/main/java/com/classnote/dto/NotebookResponse.java`
- Create: `src/main/java/com/classnote/dto/NoteListResponse.java`
- Create: `src/main/java/com/classnote/dto/NoteDetailResponse.java`
- Create: `src/main/java/com/classnote/dto/TagResponse.java`
- Create: `src/main/java/com/classnote/dto/AttachmentResponse.java`
- Create: `src/main/java/com/classnote/dto/DraftResponse.java`
- Create: `src/main/java/com/classnote/dto/ExportResponse.java`
- Create: `src/main/java/com/classnote/dto/DeleteResult.java`

- [ ] **Step 1: Create NotebookRequest.java**

```java
package com.classnote.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class NotebookRequest {
    @NotBlank @Size(min = 1, max = 100)
    private String name;
    @Size(max = 500)
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
```

- [ ] **Step 2: Create NoteRequest.java**

```java
package com.classnote.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

public class NoteRequest {
    @NotBlank
    private String notebookId;
    @NotBlank @Size(min = 1, max = 200)
    private String title;
    private String content;
    private List<String> tagIds;

    public String getNotebookId() { return notebookId; }
    public void setNotebookId(String notebookId) { this.notebookId = notebookId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getTagIds() { return tagIds; }
    public void setTagIds(List<String> tagIds) { this.tagIds = tagIds; }
}
```

- [ ] **Step 3: Create TagRequest.java**

```java
package com.classnote.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class TagRequest {
    @NotBlank @Size(min = 1, max = 30)
    private String name;
    private String color;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
```

- [ ] **Step 4: Create BatchDeleteRequest.java**

```java
package com.classnote.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

public class BatchDeleteRequest {
    @NotEmpty @Size(max = 100)
    private List<String> ids;

    public List<String> getIds() { return ids; }
    public void setIds(List<String> ids) { this.ids = ids; }
}
```

- [ ] **Step 5: Create DraftRequest.java**

```java
package com.classnote.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class DraftRequest {
    private String noteId;
    @NotBlank @Size(min = 1, max = 200)
    private String title;
    private String content;
    private String notebookId;

    public String getNoteId() { return noteId; }
    public void setNoteId(String noteId) { this.noteId = noteId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getNotebookId() { return notebookId; }
    public void setNotebookId(String notebookId) { this.notebookId = notebookId; }
}
```

- [ ] **Step 6: Create PreferenceRequest.java**

```java
package com.classnote.dto;

public class PreferenceRequest {
    private String theme;
    private Integer autoSaveInterval;
    private Integer editorFontSize;
    private String attachmentRootPath;

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public Integer getAutoSaveInterval() { return autoSaveInterval; }
    public void setAutoSaveInterval(Integer autoSaveInterval) { this.autoSaveInterval = autoSaveInterval; }
    public Integer getEditorFontSize() { return editorFontSize; }
    public void setEditorFontSize(Integer editorFontSize) { this.editorFontSize = editorFontSize; }
    public String getAttachmentRootPath() { return attachmentRootPath; }
    public void setAttachmentRootPath(String attachmentRootPath) { this.attachmentRootPath = attachmentRootPath; }
}
```

- [ ] **Step 7: Create NotebookResponse.java**

```java
package com.classnote.dto;

import com.classnote.entity.Notebook;
import java.time.LocalDateTime;

public class NotebookResponse {
    private String id;
    private String name;
    private String description;
    private int noteCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NotebookResponse from(Notebook nb) {
        NotebookResponse r = new NotebookResponse();
        r.id = nb.getId();
        r.name = nb.getName();
        r.description = nb.getDescription();
        r.noteCount = nb.getNotes().size();
        r.createdAt = nb.getCreatedAt();
        r.updatedAt = nb.getUpdatedAt();
        return r;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getNoteCount() { return noteCount; }
    public void setNoteCount(int noteCount) { this.noteCount = noteCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

- [ ] **Step 8: Create NoteListResponse.java**

```java
package com.classnote.dto;

import com.classnote.entity.Note;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class NoteListResponse {
    private String id;
    private String notebookId;
    private String notebookName;
    private String title;
    private String plainText;
    private List<TagResponse> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NoteListResponse from(Note note) {
        NoteListResponse r = new NoteListResponse();
        r.id = note.getId();
        r.notebookId = note.getNotebook().getId();
        r.notebookName = note.getNotebook().getName();
        r.title = note.getTitle();
        r.plainText = note.getPlainText() != null && note.getPlainText().length() > 200
                ? note.getPlainText().substring(0, 200) : note.getPlainText();
        r.tags = note.getTags().stream().map(TagResponse::from).collect(Collectors.toList());
        r.createdAt = note.getCreatedAt();
        r.updatedAt = note.getUpdatedAt();
        return r;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNotebookId() { return notebookId; }
    public void setNotebookId(String notebookId) { this.notebookId = notebookId; }
    public String getNotebookName() { return notebookName; }
    public void setNotebookName(String notebookName) { this.notebookName = notebookName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPlainText() { return plainText; }
    public void setPlainText(String plainText) { this.plainText = plainText; }
    public List<TagResponse> getTags() { return tags; }
    public void setTags(List<TagResponse> tags) { this.tags = tags; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

- [ ] **Step 9: Create NoteDetailResponse.java**

```java
package com.classnote.dto;

import com.classnote.entity.Note;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class NoteDetailResponse {
    private String id;
    private String notebookId;
    private String notebookName;
    private String title;
    private String content;
    private String plainText;
    private List<TagResponse> tags;
    private List<AttachmentResponse> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NoteDetailResponse from(Note note) {
        NoteDetailResponse r = new NoteDetailResponse();
        r.id = note.getId();
        r.notebookId = note.getNotebook().getId();
        r.notebookName = note.getNotebook().getName();
        r.title = note.getTitle();
        r.content = note.getContent();
        r.plainText = note.getPlainText();
        r.tags = note.getTags().stream().map(TagResponse::from).collect(Collectors.toList());
        r.attachments = note.getAttachments().stream().map(AttachmentResponse::from).collect(Collectors.toList());
        r.createdAt = note.getCreatedAt();
        r.updatedAt = note.getUpdatedAt();
        return r;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNotebookId() { return notebookId; }
    public void setNotebookId(String notebookId) { this.notebookId = notebookId; }
    public String getNotebookName() { return notebookName; }
    public void setNotebookName(String notebookName) { this.notebookName = notebookName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getPlainText() { return plainText; }
    public void setPlainText(String plainText) { this.plainText = plainText; }
    public List<TagResponse> getTags() { return tags; }
    public void setTags(List<TagResponse> tags) { this.tags = tags; }
    public List<AttachmentResponse> getAttachments() { return attachments; }
    public void setAttachments(List<AttachmentResponse> attachments) { this.attachments = attachments; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

- [ ] **Step 10: Create TagResponse.java**

```java
package com.classnote.dto;

import com.classnote.entity.Tag;
import java.time.LocalDateTime;

public class TagResponse {
    private String id;
    private String name;
    private String color;
    private long noteCount;
    private LocalDateTime createdAt;

    public static TagResponse from(Tag tag) {
        TagResponse r = new TagResponse();
        r.id = tag.getId();
        r.name = tag.getName();
        r.color = tag.getColor();
        r.createdAt = tag.getCreatedAt();
        return r;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public long getNoteCount() { return noteCount; }
    public void setNoteCount(long noteCount) { this.noteCount = noteCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

- [ ] **Step 11: Create AttachmentResponse.java**

```java
package com.classnote.dto;

import com.classnote.entity.Attachment;

public class AttachmentResponse {
    private String id;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String mimeType;

    public static AttachmentResponse from(Attachment att) {
        AttachmentResponse r = new AttachmentResponse();
        r.id = att.getId();
        r.fileName = att.getFileName();
        r.filePath = att.getFilePath();
        r.fileSize = att.getFileSize();
        r.mimeType = att.getMimeType();
        return r;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
}
```

- [ ] **Step 12: Create DraftResponse.java**

```java
package com.classnote.dto;

import com.classnote.entity.Draft;
import java.time.LocalDateTime;

public class DraftResponse {
    private String id;
    private String noteId;
    private String title;
    private String content;
    private String notebookId;
    private LocalDateTime savedAt;

    public static DraftResponse from(Draft draft) {
        DraftResponse r = new DraftResponse();
        r.id = draft.getId();
        r.noteId = draft.getNoteId();
        r.title = draft.getTitle();
        r.content = draft.getContent();
        r.notebookId = draft.getNotebookId();
        r.savedAt = draft.getSavedAt();
        return r;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNoteId() { return noteId; }
    public void setNoteId(String noteId) { this.noteId = noteId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getNotebookId() { return notebookId; }
    public void setNotebookId(String notebookId) { this.notebookId = notebookId; }
    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
}
```

- [ ] **Step 13: Create ExportResponse.java**

```java
package com.classnote.dto;

public class ExportResponse {
    private String exportPath;
    private String exportDir;
    private int notesExported;
    private int attachmentsCopied;

    public static ExportResponse forNote(String path, int copied) {
        ExportResponse r = new ExportResponse();
        r.exportPath = path;
        r.attachmentsCopied = copied;
        return r;
    }

    public static ExportResponse forNotebook(String dir, int notes, int copied) {
        ExportResponse r = new ExportResponse();
        r.exportDir = dir;
        r.notesExported = notes;
        r.attachmentsCopied = copied;
        return r;
    }

    public String getExportPath() { return exportPath; }
    public void setExportPath(String exportPath) { this.exportPath = exportPath; }
    public String getExportDir() { return exportDir; }
    public void setExportDir(String exportDir) { this.exportDir = exportDir; }
    public int getNotesExported() { return notesExported; }
    public void setNotesExported(int notesExported) { this.notesExported = notesExported; }
    public int getAttachmentsCopied() { return attachmentsCopied; }
    public void setAttachmentsCopied(int attachmentsCopied) { this.attachmentsCopied = attachmentsCopied; }
}
```

- [ ] **Step 14: Create DeleteResult.java**

```java
package com.classnote.dto;

public class DeleteResult {
    private int deletedCount;
    private int cleanedAttachments;

    public DeleteResult(int deletedCount, int cleanedAttachments) {
        this.deletedCount = deletedCount;
        this.cleanedAttachments = cleanedAttachments;
    }

    public int getDeletedCount() { return deletedCount; }
    public void setDeletedCount(int deletedCount) { this.deletedCount = deletedCount; }
    public int getCleanedAttachments() { return cleanedAttachments; }
    public void setCleanedAttachments(int cleanedAttachments) { this.cleanedAttachments = cleanedAttachments; }
}
```

---

### Task 5: Exception Handling

**Files:**
- Create: `src/main/java/com/classnote/exception/ResourceNotFoundException.java`
- Create: `src/main/java/com/classnote/exception/DuplicateResourceException.java`
- Create: `src/main/java/com/classnote/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: Create ResourceNotFoundException.java**

```java
package com.classnote.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, String id) {
        super(resource + " not found: " + id);
    }
}
```

- [ ] **Step 2: Create DuplicateResourceException.java**

```java
package com.classnote.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
```

- [ ] **Step 3: Create GlobalExceptionHandler.java**

```java
package com.classnote.exception;

import com.classnote.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<?> handleNotFound(ResourceNotFoundException e) {
        return ApiResponse.error(404, e.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<?> handleDuplicate(DuplicateResourceException e) {
        return ApiResponse.error(409, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b).orElse("Invalid input");
        return ApiResponse.error(400, msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleGeneral(Exception e) {
        return ApiResponse.error(500, "Internal server error: " + e.getMessage());
    }
}
```

---

### Task 6: Service Layer

**Files:**
- Create: `src/main/java/com/classnote/service/NotebookService.java`
- Create: `src/main/java/com/classnote/service/NoteService.java`
- Create: `src/main/java/com/classnote/service/TagService.java`
- Create: `src/main/java/com/classnote/service/AttachmentService.java`
- Create: `src/main/java/com/classnote/service/DraftService.java`
- Create: `src/main/java/com/classnote/service/PreferenceService.java`
- Create: `src/main/java/com/classnote/service/ExportService.java`
- Create: `src/main/java/com/classnote/service/SearchService.java`

- [ ] **Step 1: Create NotebookService.java**

```java
package com.classnote.service;

import com.classnote.dto.NotebookRequest;
import com.classnote.entity.Notebook;
import com.classnote.exception.ResourceNotFoundException;
import com.classnote.repository.NoteRepository;
import com.classnote.repository.NotebookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotebookService {

    private final NotebookRepository notebookRepo;
    private final NoteRepository noteRepo;

    public NotebookService(NotebookRepository notebookRepo, NoteRepository noteRepo) {
        this.notebookRepo = notebookRepo;
        this.noteRepo = noteRepo;
    }

    public Notebook create(NotebookRequest req) {
        Notebook nb = new Notebook();
        nb.setName(req.getName());
        nb.setDescription(req.getDescription());
        return notebookRepo.save(nb);
    }

    public Page<Notebook> list(int page, int size, String sort) {
        String[] parts = sort.split(",");
        Sort.Direction dir = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return notebookRepo.findAll(PageRequest.of(page, size, Sort.by(dir, parts[0])));
    }

    public Notebook getById(String id) {
        return notebookRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notebook", id));
    }

    public Notebook update(String id, NotebookRequest req) {
        Notebook nb = getById(id);
        if (req.getName() != null) nb.setName(req.getName());
        if (req.getDescription() != null) nb.setDescription(req.getDescription());
        return notebookRepo.save(nb);
    }

    @Transactional
    public int delete(String id) {
        Notebook nb = getById(id);
        int noteCount = nb.getNotes().size();
        notebookRepo.delete(nb);
        return noteCount;
    }
}
```

- [ ] **Step 2: Create NoteService.java**

```java
package com.classnote.service;

import com.classnote.dto.BatchDeleteRequest;
import com.classnote.dto.DeleteResult;
import com.classnote.dto.NoteRequest;
import com.classnote.entity.Attachment;
import com.classnote.entity.Note;
import com.classnote.entity.Notebook;
import com.classnote.entity.Tag;
import com.classnote.exception.ResourceNotFoundException;
import com.classnote.repository.AttachmentRepository;
import com.classnote.repository.NoteRepository;
import com.classnote.repository.NotebookRepository;
import com.classnote.repository.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NoteService {

    private final NoteRepository noteRepo;
    private final NotebookRepository notebookRepo;
    private final TagRepository tagRepo;
    private final AttachmentRepository attachmentRepo;

    public NoteService(NoteRepository noteRepo, NotebookRepository notebookRepo,
                       TagRepository tagRepo, AttachmentRepository attachmentRepo) {
        this.noteRepo = noteRepo;
        this.notebookRepo = notebookRepo;
        this.tagRepo = tagRepo;
        this.attachmentRepo = attachmentRepo;
    }

    @Transactional
    public Note create(NoteRequest req) {
        Notebook notebook = notebookRepo.findById(req.getNotebookId())
                .orElseThrow(() -> new ResourceNotFoundException("Notebook", req.getNotebookId()));
        Note note = new Note();
        note.setTitle(req.getTitle());
        note.setContent(req.getContent());
        note.setPlainText(stripMarkdown(req.getContent()));
        note.setNotebook(notebook);
        if (req.getTagIds() != null) {
            note.setTags(new HashSet<>(tagRepo.findAllById(req.getTagIds())));
        }
        return noteRepo.save(note);
    }

    public Page<Note> list(String notebookId, String tagId, String keyword,
                           int page, int size, String sort) {
        String[] parts = sort.split(",");
        Sort.Direction dir = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = "updatedAt".equals(parts[0]) ? "updatedAt" :
                           "title".equals(parts[0]) ? "title" : "createdAt";
        return noteRepo.search(notebookId, tagId, keyword,
                PageRequest.of(page, size, Sort.by(dir, sortField)));
    }

    public Note getById(String id) {
        Note note = noteRepo.findByIdWithTags(id);
        if (note == null) throw new ResourceNotFoundException("Note", id);
        note.getAttachments().size();
        return note;
    }

    @Transactional
    public Note update(String id, NoteRequest req) {
        Note note = getById(id);
        if (req.getNotebookId() != null) {
            Notebook nb = notebookRepo.findById(req.getNotebookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Notebook", req.getNotebookId()));
            note.setNotebook(nb);
        }
        if (req.getTitle() != null) note.setTitle(req.getTitle());
        if (req.getContent() != null) {
            note.setContent(req.getContent());
            note.setPlainText(stripMarkdown(req.getContent()));
        }
        if (req.getTagIds() != null) {
            note.setTags(new HashSet<>(tagRepo.findAllById(req.getTagIds())));
        }
        return noteRepo.save(note);
    }

    @Transactional
    public void delete(String id) {
        Note note = getById(id);
        cleanupAttachments(note);
        noteRepo.delete(note);
    }

    @Transactional
    public DeleteResult batchDelete(BatchDeleteRequest req) {
        List<Note> notes = noteRepo.findAllById(req.getIds());
        int cleanedAttachments = 0;
        for (Note note : notes) {
            cleanedAttachments += cleanupAttachments(note);
        }
        noteRepo.deleteAllInBatch(notes);
        return new DeleteResult(notes.size(), cleanedAttachments);
    }

    public long countByNotebook(String notebookId) {
        return noteRepo.countByNotebookId(notebookId);
    }

    private int cleanupAttachments(Note note) {
        int cleaned = 0;
        for (Attachment att : note.getAttachments()) {
            boolean referenced = attachmentRepo.existsByFilePathAndNoteIdNot(att.getFilePath(), note.getId());
            if (!referenced) {
                try {
                    Files.deleteIfExists(Paths.get(att.getFilePath()));
                    cleaned++;
                } catch (IOException ignored) {}
            }
        }
        return cleaned;
    }

    private String stripMarkdown(String md) {
        if (md == null) return "";
        return md.replaceAll("#+\\s", "")
                 .replaceAll("\\*\\*?(.*?)\\*\\*?", "$1")
                 .replaceAll("!\\[.*?\\]\\(.*?\\)", "")
                 .replaceAll("\\[(.*?)\\]\\(.*?\\)", "$1")
                 .replaceAll("`{1,3}.*?`{1,3}", "")
                 .replaceAll("[-*+]\\s", "")
                 .replaceAll("\\n+", " ")
                 .trim();
    }
}
```

- [ ] **Step 3: Create TagService.java**

```java
package com.classnote.service;

import com.classnote.dto.TagRequest;
import com.classnote.entity.Tag;
import com.classnote.exception.DuplicateResourceException;
import com.classnote.exception.ResourceNotFoundException;
import com.classnote.repository.TagRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepo;

    public TagService(TagRepository tagRepo) {
        this.tagRepo = tagRepo;
    }

    public Tag create(TagRequest req) {
        if (tagRepo.existsByName(req.getName())) {
            throw new DuplicateResourceException("Tag name already exists: " + req.getName());
        }
        Tag tag = new Tag();
        tag.setName(req.getName());
        tag.setColor(req.getColor());
        return tagRepo.save(tag);
    }

    public List<Tag> list(String sort) {
        String[] parts = sort.split(",");
        Sort.Direction dir = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return tagRepo.findAll(Sort.by(dir, parts[0]));
    }

    public Tag getById(String id) {
        return tagRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
    }

    @Transactional
    public Tag update(String id, TagRequest req) {
        Tag tag = getById(id);
        if (req.getName() != null) {
            if (!tag.getName().equals(req.getName()) && tagRepo.existsByName(req.getName())) {
                throw new DuplicateResourceException("Tag name already exists: " + req.getName());
            }
            tag.setName(req.getName());
        }
        if (req.getColor() != null) tag.setColor(req.getColor());
        return tagRepo.save(tag);
    }

    @Transactional
    public long delete(String id) {
        Tag tag = getById(id);
        long noteCount = tagRepo.countNotesByTagId(id);
        tagRepo.delete(tag);
        return noteCount;
    }
}
```

- [ ] **Step 4: Create AttachmentService.java**

```java
package com.classnote.service;

import com.classnote.config.FileStorageConfig;
import com.classnote.entity.Attachment;
import com.classnote.entity.Note;
import com.classnote.exception.ResourceNotFoundException;
import com.classnote.repository.AttachmentRepository;
import com.classnote.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepo;
    private final NoteRepository noteRepo;
    private final FileStorageConfig fileStorageConfig;

    public AttachmentService(AttachmentRepository attachmentRepo,
                             NoteRepository noteRepo,
                             FileStorageConfig fileStorageConfig) {
        this.attachmentRepo = attachmentRepo;
        this.noteRepo = noteRepo;
        this.fileStorageConfig = fileStorageConfig;
    }

    @Transactional
    public Attachment upload(MultipartFile file, String noteId) throws IOException {
        Note note = noteRepo.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", noteId));

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        Path uploadDir = fileStorageConfig.getAttachmentRoot().resolve(datePath);
        Files.createDirectories(uploadDir);

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString() + ext;
        Path targetPath = uploadDir.resolve(storedName);
        file.transferTo(targetPath.toFile());

        Attachment att = new Attachment();
        att.setFileName(originalName);
        att.setFilePath(targetPath.toString());
        att.setFileSize(file.getSize());
        att.setMimeType(file.getContentType());
        att.setNote(note);
        return attachmentRepo.save(att);
    }

    public List<Attachment> listByNote(String noteId) {
        return attachmentRepo.findByNoteId(noteId);
    }

    @Transactional
    public void delete(String id) {
        Attachment att = attachmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", id));
        String filePath = att.getFilePath();
        String noteId = att.getNote().getId();
        attachmentRepo.delete(att);

        boolean stillReferenced = attachmentRepo.existsByFilePathAndNoteIdNot(filePath, noteId);
        if (!stillReferenced) {
            try {
                Files.deleteIfExists(Path.of(filePath));
            } catch (IOException ignored) {}
        }
    }
}
```

- [ ] **Step 5: Create DraftService.java**

```java
package com.classnote.service;

import com.classnote.dto.DraftRequest;
import com.classnote.entity.Draft;
import com.classnote.repository.DraftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DraftService {

    private final DraftRepository draftRepo;

    public DraftService(DraftRepository draftRepo) {
        this.draftRepo = draftRepo;
    }

    @Transactional
    public Draft save(DraftRequest req) {
        draftRepo.deleteAll();
        Draft draft = new Draft();
        draft.setNoteId(req.getNoteId());
        draft.setTitle(req.getTitle());
        draft.setContent(req.getContent());
        draft.setNotebookId(req.getNotebookId());
        return draftRepo.save(draft);
    }

    public Draft getLatest() {
        return draftRepo.findTopByOrderBySavedAtDesc();
    }

    @Transactional
    public void delete() {
        draftRepo.deleteAll();
    }
}
```

- [ ] **Step 6: Create PreferenceService.java**

```java
package com.classnote.service;

import com.classnote.dto.PreferenceRequest;
import com.classnote.entity.UserPreference;
import com.classnote.repository.UserPreferenceRepository;
import org.springframework.stereotype.Service;

@Service
public class PreferenceService {

    private final UserPreferenceRepository prefRepo;

    public PreferenceService(UserPreferenceRepository prefRepo) {
        this.prefRepo = prefRepo;
    }

    public UserPreference get() {
        return prefRepo.findById("default").orElseGet(() -> prefRepo.save(new UserPreference()));
    }

    public UserPreference update(PreferenceRequest req) {
        UserPreference pref = get();
        if (req.getTheme() != null) pref.setTheme(req.getTheme());
        if (req.getAutoSaveInterval() != null) pref.setAutoSaveInterval(req.getAutoSaveInterval());
        if (req.getEditorFontSize() != null) pref.setEditorFontSize(req.getEditorFontSize());
        if (req.getAttachmentRootPath() != null) pref.setAttachmentRootPath(req.getAttachmentRootPath());
        return prefRepo.save(pref);
    }
}
```

- [ ] **Step 7: Create ExportService.java**

```java
package com.classnote.service;

import com.classnote.dto.ExportResponse;
import com.classnote.entity.Attachment;
import com.classnote.entity.Note;
import com.classnote.entity.Notebook;
import com.classnote.exception.ResourceNotFoundException;
import com.classnote.repository.NotebookRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class ExportService {

    private final NotebookRepository notebookRepo;
    private final NoteService noteService;

    public ExportService(NotebookRepository notebookRepo, NoteService noteService) {
        this.notebookRepo = notebookRepo;
        this.noteService = noteService;
    }

    public ExportResponse exportNote(String noteId, String targetDir) throws IOException {
        Note note = noteService.getById(noteId);
        Path dir = Paths.get(targetDir);
        Files.createDirectories(dir);

        String safeName = sanitizeFileName(note.getTitle()) + ".md";
        Path mdFile = dir.resolve(safeName);
        Files.writeString(mdFile, note.getContent() != null ? note.getContent() : "");

        int copied = copyAttachments(note, dir.resolve("attachments"));
        return ExportResponse.forNote(mdFile.toString(), copied);
    }

    public ExportResponse exportNotebook(String notebookId, String targetDir) throws IOException {
        Notebook nb = notebookRepo.findById(notebookId)
                .orElseThrow(() -> new ResourceNotFoundException("Notebook", notebookId));

        Path nbDir = Paths.get(targetDir, sanitizeFileName(nb.getName()));
        Files.createDirectories(nbDir);

        int totalCopied = 0;
        int exported = 0;
        for (Note note : nb.getNotes()) {
            String safeName = sanitizeFileName(note.getTitle()) + ".md";
            Files.writeString(nbDir.resolve(safeName),
                    note.getContent() != null ? note.getContent() : "");
            totalCopied += copyAttachments(note, nbDir.resolve("attachments"));
            exported++;
        }
        return ExportResponse.forNotebook(nbDir.toString(), exported, totalCopied);
    }

    private int copyAttachments(Note note, Path attachmentsDir) throws IOException {
        int count = 0;
        for (Attachment att : note.getAttachments()) {
            Path src = Paths.get(att.getFilePath());
            if (Files.exists(src)) {
                Files.createDirectories(attachmentsDir);
                Files.copy(src, attachmentsDir.resolve(att.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                count++;
            }
        }
        return count;
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }
}
```

- [ ] **Step 8: Create SearchService.java**

```java
package com.classnote.service;

import com.classnote.dto.NoteListResponse;
import com.classnote.entity.Note;
import com.classnote.repository.NoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final NoteRepository noteRepo;

    public SearchService(NoteRepository noteRepo) {
        this.noteRepo = noteRepo;
    }

    public Page<NoteListResponse> search(String keyword, String notebookId, String tagId,
                                          int page, int size) {
        Page<Note> notes = noteRepo.search(notebookId, tagId,
                keyword != null ? keyword.trim() : null,
                PageRequest.of(page, size));
        return notes.map(note -> {
            NoteListResponse r = NoteListResponse.from(note);
            if (keyword != null && !keyword.isEmpty()) {
                String kw = keyword.trim();
                r.setTitle(highlight(r.getTitle(), kw));
                r.setPlainText(highlight(r.getPlainText(), kw));
            }
            return r;
        });
    }

    private String highlight(String text, String keyword) {
        if (text == null || keyword == null) return text;
        return text.replaceAll("(?i)(" + java.util.regex.Pattern.quote(keyword) + ")", "<em>$1</em>");
    }
}
```

---

### Task 7: Controllers

**Files:**
- Create: `src/main/java/com/classnote/controller/NotebookController.java`
- Create: `src/main/java/com/classnote/controller/NoteController.java`
- Create: `src/main/java/com/classnote/controller/TagController.java`
- Create: `src/main/java/com/classnote/controller/AttachmentController.java`
- Create: `src/main/java/com/classnote/controller/DraftController.java`
- Create: `src/main/java/com/classnote/controller/PreferenceController.java`
- Create: `src/main/java/com/classnote/controller/ConfigController.java`
- Create: `src/main/java/com/classnote/controller/ExportController.java`
- Create: `src/main/java/com/classnote/controller/SearchController.java`

- [ ] **Step 1: Create NotebookController.java**

```java
package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.NotebookRequest;
import com.classnote.dto.NotebookResponse;
import com.classnote.entity.Notebook;
import com.classnote.service.NotebookService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/notebooks")
public class NotebookController {

    private final NotebookService notebookService;

    public NotebookController(NotebookService notebookService) {
        this.notebookService = notebookService;
    }

    @PostMapping
    public ApiResponse<NotebookResponse> create(@Valid @RequestBody NotebookRequest req) {
        Notebook nb = notebookService.create(req);
        return ApiResponse.success("创建成功", NotebookResponse.from(nb));
    }

    @GetMapping
    public ApiResponse<Page<NotebookResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort) {
        page = Math.max(0, page - 1);
        size = Math.min(size, 50);
        return ApiResponse.success(notebookService.list(page, size, sort).map(NotebookResponse::from));
    }

    @GetMapping("/{id}")
    public ApiResponse<NotebookResponse> get(@PathVariable String id) {
        return ApiResponse.success(NotebookResponse.from(notebookService.getById(id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<NotebookResponse> update(@PathVariable String id, @Valid @RequestBody NotebookRequest req) {
        return ApiResponse.success(NotebookResponse.from(notebookService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable String id) {
        int count = notebookService.delete(id);
        return ApiResponse.success("删除成功，已同时删除 " + count + " 篇笔记", null);
    }
}
```

- [ ] **Step 2: Create NoteController.java**

```java
package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.*;
import com.classnote.entity.Note;
import com.classnote.service.NoteService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
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
}
```

- [ ] **Step 3: Create TagController.java**

```java
package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.TagRequest;
import com.classnote.dto.TagResponse;
import com.classnote.entity.Tag;
import com.classnote.service.TagService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public ApiResponse<TagResponse> create(@Valid @RequestBody TagRequest req) {
        Tag tag = tagService.create(req);
        return ApiResponse.success("创建成功", TagResponse.from(tag));
    }

    @GetMapping
    public ApiResponse<List<TagResponse>> list(
            @RequestParam(defaultValue = "name,asc") String sort) {
        List<TagResponse> tags = tagService.list(sort).stream().map(t -> {
            TagResponse r = TagResponse.from(t);
            r.setNoteCount(tagService.getById(t.getId()) != null ?
                    tagService.list(sort).size() : 0);
            return r;
        }).collect(Collectors.toList());

        List<TagResponse> enriched = tagService.list(sort).stream().map(t -> {
            TagResponse r = TagResponse.from(t);
            return r;
        }).collect(Collectors.toList());
        return ApiResponse.success(enriched);
    }

    @PutMapping("/{id}")
    public ApiResponse<TagResponse> update(@PathVariable String id, @Valid @RequestBody TagRequest req) {
        return ApiResponse.success(TagResponse.from(tagService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable String id) {
        long count = tagService.delete(id);
        return ApiResponse.success("删除成功，已解除与 " + count + " 篇笔记的关联", null);
    }
}
```

- [ ] **Step 4: Create AttachmentController.java**

```java
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
        data.put("markdownRef", "![" + att.getFileName() + "](attachments/" +
                att.getFilePath().substring(att.getFilePath().indexOf("classnote-files") + 16).replace("\\", "/") + ")");
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
```

- [ ] **Step 5: Create DraftController.java**

```java
package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.DraftRequest;
import com.classnote.dto.DraftResponse;
import com.classnote.entity.Draft;
import com.classnote.service.DraftService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/drafts")
public class DraftController {

    private final DraftService draftService;

    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }

    @PostMapping
    public ApiResponse<DraftResponse> save(@Valid @RequestBody DraftRequest req) {
        Draft draft = draftService.save(req);
        return ApiResponse.success("草稿已保存", DraftResponse.from(draft));
    }

    @GetMapping("/latest")
    public ApiResponse<DraftResponse> getLatest() {
        Draft draft = draftService.getLatest();
        return ApiResponse.success(draft != null ? DraftResponse.from(draft) : null);
    }

    @DeleteMapping
    public ApiResponse<?> delete() {
        draftService.delete();
        return ApiResponse.success("草稿已清除", null);
    }
}
```

- [ ] **Step 6: Create PreferenceController.java**

```java
package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.PreferenceRequest;
import com.classnote.entity.UserPreference;
import com.classnote.service.PreferenceService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/preferences")
public class PreferenceController {

    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping
    public ApiResponse<UserPreference> get() {
        return ApiResponse.success(preferenceService.get());
    }

    @PutMapping
    public ApiResponse<UserPreference> update(@RequestBody PreferenceRequest req) {
        return ApiResponse.success(preferenceService.update(req));
    }
}
```

- [ ] **Step 7: Create ConfigController.java**

```java
package com.classnote.controller;

import com.classnote.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
public class ConfigController {

    private final DataSource dataSource;

    @Value("${app.version}")
    private String version;

    @Value("${app.data-directory:~/.classnote}")
    private String dataDirectory;

    @Value("${app.attachment-root-path:~/classnote-files}")
    private String attachmentRootPath;

    public ConfigController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("version", version);
        config.put("dataDirectory", dataDirectory);
        config.put("attachmentRootPath", attachmentRootPath);
        config.put("maxUploadSize", 52428800);
        config.put("dbConnected", checkDb());
        return ApiResponse.success(config);
    }

    @GetMapping("/db-status")
    public ApiResponse<Map<String, Object>> dbStatus() {
        Map<String, Object> status = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            status.put("connected", true);
            status.put("url", conn.getMetaData().getURL());
            status.put("version", conn.getMetaData().getDatabaseProductVersion());
        } catch (Exception e) {
            status.put("connected", false);
            status.put("error", e.getMessage());
        }
        return ApiResponse.success(status);
    }

    private boolean checkDb() {
        try (Connection conn = dataSource.getConnection()) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

- [ ] **Step 8: Create ExportController.java**

```java
package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.ExportResponse;
import com.classnote.service.ExportService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/notes/{id}")
    public ApiResponse<ExportResponse> exportNote(
            @PathVariable String id,
            @RequestParam String targetDir) throws IOException {
        return ApiResponse.success("导出成功", exportService.exportNote(id, targetDir));
    }

    @GetMapping("/notebooks/{id}")
    public ApiResponse<ExportResponse> exportNotebook(
            @PathVariable String id,
            @RequestParam String targetDir) throws IOException {
        return ApiResponse.success("导出成功", exportService.exportNotebook(id, targetDir));
    }
}
```

- [ ] **Step 9: Create SearchController.java**

```java
package com.classnote.controller;

import com.classnote.common.ApiResponse;
import com.classnote.dto.NoteListResponse;
import com.classnote.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ApiResponse<Page<NoteListResponse>> search(
            @RequestParam String keyword,
            @RequestParam(required = false) String notebookId,
            @RequestParam(required = false) String tagId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ApiResponse.error(400, "关键词不能为空");
        }
        page = Math.max(0, page - 1);
        size = Math.min(size, 50);
        return ApiResponse.success(searchService.search(keyword, notebookId, tagId, page, size));
    }
}
```

---

### Task 8: Fix TagController Note Count

**Files:**
- Modify: `src/main/java/com/classnote/controller/TagController.java`

The TagController needs to properly compute noteCount. Replace the list method:

- [ ] **Step 1: Fix TagController list method**

Replace the GET /tags method in TagController.java with:

```java
@GetMapping
public ApiResponse<List<TagResponse>> list(@RequestParam(defaultValue = "name,asc") String sort) {
    List<Tag> tags = tagService.list(sort);
    List<TagResponse> enriched = tags.stream().map(t -> {
        TagResponse r = TagResponse.from(t);
        r.setNoteCount(tagService.countNotesByTag(t.getId()));
        return r;
    }).collect(Collectors.toList());
    return ApiResponse.success(enriched);
}
```

Also add `countNotesByTag` method to TagService.java:

```java
public long countNotesByTag(String tagId) {
    return tagRepo.countNotesByTagId(tagId);
}
```

---

### Task 9: Frontend Project Setup

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.js`
- Create: `frontend/index.html`
- Create: `frontend/src/main.js`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/styles/global.css`

- [ ] **Step 1: Create package.json**

```json
{
  "name": "classnote-frontend",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.0",
    "element-plus": "^2.7.0",
    "@element-plus/icons-vue": "^2.3.0",
    "axios": "^1.7.0",
    "marked": "^12.0.0",
    "codemirror": "^5.65.0",
    "codemirror-editor-vue3": "^2.5.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.2.0"
  }
}
```

- [ ] **Step 2: Create vite.config.js**

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080'
    }
  },
  build: {
    outDir: '../src/main/resources/static'
  }
})
```

- [ ] **Step 3: Create index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>ClassNote Local</title>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.js"></script>
</body>
</html>
```

- [ ] **Step 4: Create main.js**

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './styles/global.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: { el: { } } })

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
```

- [ ] **Step 5: Create App.vue**

```vue
<template>
  <router-view />
</template>

<script>
export default { name: 'App' }
</script>
```

- [ ] **Step 6: Create global.css**

```css
* { margin: 0; padding: 0; box-sizing: border-box; }
html, body, #app { height: 100%; font-family: 'Helvetica Neue', Arial, sans-serif; }
body.dark { background: #1a1a2e; color: #e0e0e0; }
body.dark .el-card { background: #16213e; border-color: #0f3460; color: #e0e0e0; }
body.dark .el-dialog { background: #16213e; }
body.dark .el-input__inner { background: #0f3460; border-color: #533483; color: #e0e0e0; }
body.dark .CodeMirror { background: #0f3460 !important; color: #e0e0e0 !important; }
```

---

### Task 10: Frontend API Layer

**Files:**
- Create: `frontend/src/api/index.js`
- Create: `frontend/src/api/notebooks.js`
- Create: `frontend/src/api/notes.js`
- Create: `frontend/src/api/tags.js`
- Create: `frontend/src/api/search.js`
- Create: `frontend/src/api/attachments.js`
- Create: `frontend/src/api/drafts.js`
- Create: `frontend/src/api/preferences.js`
- Create: `frontend/src/api/export.js`

- [ ] **Step 1: Create api/index.js**

```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api/v1',
  timeout: 10000
})

api.interceptors.response.use(
  res => {
    const data = res.data
    if (data.code !== 200) {
      ElMessage.error(data.message || 'Request failed')
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  err => {
    ElMessage.error(err.response?.data?.message || err.message)
    return Promise.reject(err)
  }
)

export default api
```

- [ ] **Step 2: Create api/notebooks.js**

```javascript
import api from './index'

export const notebookApi = {
  list(params) { return api.get('/notebooks', { params }) },
  get(id) { return api.get(`/notebooks/${id}`) },
  create(data) { return api.post('/notebooks', data) },
  update(id, data) { return api.put(`/notebooks/${id}`, data) },
  delete(id) { return api.delete(`/notebooks/${id}`) }
}
```

- [ ] **Step 3: Create api/notes.js**

```javascript
import api from './index'

export const noteApi = {
  list(params) { return api.get('/notes', { params }) },
  get(id) { return api.get(`/notes/${id}`) },
  create(data) { return api.post('/notes', data) },
  update(id, data) { return api.put(`/notes/${id}`, data) },
  delete(id) { return api.delete(`/notes/${id}`) },
  batchDelete(ids) { return api.delete('/notes/batch', { data: { ids } }) }
}
```

- [ ] **Step 4: Create api/tags.js**

```javascript
import api from './index'

export const tagApi = {
  list(params) { return api.get('/tags', { params }) },
  create(data) { return api.post('/tags', data) },
  update(id, data) { return api.put(`/tags/${id}`, data) },
  delete(id) { return api.delete(`/tags/${id}`) }
}
```

- [ ] **Step 5: Create api/search.js**

```javascript
import api from './index'

export const searchApi = {
  search(params) { return api.get('/search', { params }) }
}
```

- [ ] **Step 6: Create api/attachments.js**

```javascript
import api from './index'

export const attachmentApi = {
  upload(formData) { return api.post('/attachments/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } }) },
  listByNote(noteId) { return api.get(`/notes/${noteId}/attachments`) },
  delete(id) { return api.delete(`/attachments/${id}`) }
}
```

- [ ] **Step 7: Create api/drafts.js**

```javascript
import api from './index'

export const draftApi = {
  save(data) { return api.post('/drafts', data) },
  getLatest() { return api.get('/drafts/latest') },
  delete() { return api.delete('/drafts') }
}
```

- [ ] **Step 8: Create api/preferences.js**

```javascript
import api from './index'

export const preferenceApi = {
  get() { return api.get('/preferences') },
  update(data) { return api.put('/preferences', data) }
}
```

- [ ] **Step 9: Create api/export.js**

```javascript
import api from './index'

export const exportApi = {
  exportNote(id, targetDir) { return api.get(`/export/notes/${id}`, { params: { targetDir } }) },
  exportNotebook(id, targetDir) { return api.get(`/export/notebooks/${id}`, { params: { targetDir } }) }
}
```

---

### Task 11: Vue Router

**Files:**
- Create: `frontend/src/router/index.js`

- [ ] **Step 1: Create router/index.js**

```javascript
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../components/AppLayout.vue'),
    redirect: '/notes',
    children: [
      {
        path: 'notes',
        name: 'NoteList',
        component: () => import('../views/NotebookView.vue')
      },
      {
        path: 'notes/:id/edit',
        name: 'NoteEditor',
        component: () => import('../views/NoteEditor.vue'),
        props: true
      },
      {
        path: 'notes/new',
        name: 'NewNote',
        component: () => import('../views/NoteEditor.vue')
      },
      {
        path: 'tags',
        name: 'TagManager',
        component: () => import('../views/TagManager.vue')
      },
      {
        path: 'search',
        name: 'SearchView',
        component: () => import('../views/SearchView.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
```

---

### Task 12: Pinia Stores

**Files:**
- Create: `frontend/src/stores/notebook.js`
- Create: `frontend/src/stores/note.js`
- Create: `frontend/src/stores/tag.js`
- Create: `frontend/src/stores/draft.js`
- Create: `frontend/src/stores/preference.js`

- [ ] **Step 1: Create stores/notebook.js**

```javascript
import { defineStore } from 'pinia'
import { notebookApi } from '../api/notebooks'

export const useNotebookStore = defineStore('notebook', {
  state: () => ({
    notebooks: [],
    loading: false,
    total: 0
  }),
  actions: {
    async fetchNotebooks(params = {}) {
      this.loading = true
      try {
        const res = await notebookApi.list(params)
        this.notebooks = res.data.content || []
        this.total = res.data.totalElements || 0
      } finally {
        this.loading = false
      }
    },
    async create(data) {
      await notebookApi.create(data)
      await this.fetchNotebooks()
    },
    async update(id, data) {
      await notebookApi.update(id, data)
      await this.fetchNotebooks()
    },
    async remove(id) {
      await notebookApi.delete(id)
      await this.fetchNotebooks()
    }
  }
})
```

- [ ] **Step 2: Create stores/note.js**

```javascript
import { defineStore } from 'pinia'
import { noteApi } from '../api/notes'

export const useNoteStore = defineStore('note', {
  state: () => ({
    notes: [],
    currentNote: null,
    loading: false,
    total: 0
  }),
  actions: {
    async fetchNotes(params = {}) {
      this.loading = true
      try {
        const res = await noteApi.list(params)
        this.notes = res.data.content || []
        this.total = res.data.totalElements || 0
      } finally {
        this.loading = false
      }
    },
    async fetchNote(id) {
      const res = await noteApi.get(id)
      this.currentNote = res.data
      return this.currentNote
    },
    async create(data) {
      const res = await noteApi.create(data)
      return res.data
    },
    async update(id, data) {
      const res = await noteApi.update(id, data)
      this.currentNote = res.data
      return res.data
    },
    async remove(id) {
      await noteApi.delete(id)
      this.currentNote = null
    },
    async batchRemove(ids) {
      await noteApi.batchDelete(ids)
    }
  }
})
```

- [ ] **Step 3: Create stores/tag.js**

```javascript
import { defineStore } from 'pinia'
import { tagApi } from '../api/tags'

export const useTagStore = defineStore('tag', {
  state: () => ({
    tags: [],
    loading: false
  }),
  actions: {
    async fetchTags(params = {}) {
      this.loading = true
      try {
        const res = await tagApi.list(params)
        this.tags = res.data || []
      } finally {
        this.loading = false
      }
    },
    async create(data) {
      await tagApi.create(data)
      await this.fetchTags()
    },
    async update(id, data) {
      await tagApi.update(id, data)
      await this.fetchTags()
    },
    async remove(id) {
      await tagApi.delete(id)
      await this.fetchTags()
    }
  }
})
```

- [ ] **Step 4: Create stores/draft.js**

```javascript
import { defineStore } from 'pinia'
import { draftApi } from '../api/drafts'

export const useDraftStore = defineStore('draft', {
  state: () => ({
    draft: null
  }),
  actions: {
    async save(data) {
      const res = await draftApi.save(data)
      this.draft = res.data
    },
    async fetchLatest() {
      const res = await draftApi.getLatest()
      this.draft = res.data
    },
    async clear() {
      await draftApi.delete()
      this.draft = null
    }
  }
})
```

- [ ] **Step 5: Create stores/preference.js**

```javascript
import { defineStore } from 'pinia'
import { preferenceApi } from '../api/preferences'

export const usePreferenceStore = defineStore('preference', {
  state: () => ({
    theme: 'light',
    autoSaveInterval: 30,
    editorFontSize: 14
  }),
  actions: {
    async fetch() {
      try {
        const res = await preferenceApi.get()
        if (res.data) {
          this.theme = res.data.theme || 'light'
          this.autoSaveInterval = res.data.autoSaveInterval || 30
          this.editorFontSize = res.data.editorFontSize || 14
        }
      } catch (e) { /* use defaults */ }
    },
    async update(data) {
      const res = await preferenceApi.update(data)
      if (res.data) {
        this.theme = res.data.theme
        this.autoSaveInterval = res.data.autoSaveInterval
        this.editorFontSize = res.data.editorFontSize
      }
    },
    toggleTheme() {
      this.theme = this.theme === 'dark' ? 'light' : 'dark'
      document.body.className = this.theme
      preferenceApi.update({ theme: this.theme }).catch(() => {})
    },
    applyTheme() {
      document.body.className = this.theme
    }
  }
})
```

---

### Task 13: AppLayout & Sidebar Components

**Files:**
- Create: `frontend/src/components/AppLayout.vue`
- Create: `frontend/src/components/Sidebar.vue`

- [ ] **Step 1: Create AppLayout.vue**

```vue
<template>
  <el-container style="height:100%">
    <el-aside width="260px" style="border-right:1px solid #e4e7ed; display:flex; flex-direction:column;">
      <Sidebar />
    </el-aside>
    <el-main style="padding:0; overflow:hidden;">
      <router-view />
    </el-main>
  </el-container>
</template>

<script>
import Sidebar from './Sidebar.vue'
export default {
  name: 'AppLayout',
  components: { Sidebar }
}
</script>
```

- [ ] **Step 2: Create Sidebar.vue**

```vue
<template>
  <div style="padding:16px; border-bottom:1px solid #e4e7ed;">
    <h2 style="margin:0; color:#409EFF; font-size:18px;">ClassNote</h2>
  </div>

  <div style="padding:12px 16px;">
    <el-input v-model="searchKeyword" placeholder="搜索笔记..." size="small" clearable
      @keyup.enter="goSearch" @clear="goSearch">
      <template #prefix><el-icon><Search /></el-icon></template>
    </el-input>
  </div>

  <el-menu :default-active="activeMenu" router style="border-right:none; flex:1; overflow-y:auto;">
    <el-menu-item index="/notes">
      <el-icon><Notebook /></el-icon>
      <span>全部笔记</span>
    </el-menu-item>

    <el-sub-menu index="notebooks">
      <template #title>
        <el-icon><Folder /></el-icon>
        <span>笔记本</span>
      </template>
      <el-menu-item v-for="nb in notebookStore.notebooks" :key="nb.id"
        :index="`/notes?notebookId=${nb.id}`">
        {{ nb.name }}
        <span style="color:#909399; font-size:12px; margin-left:auto;">({{ nb.noteCount }})</span>
      </el-menu-item>
    </el-sub-menu>

    <el-sub-menu index="tags-menu">
      <template #title>
        <el-icon><PriceTag /></el-icon>
        <span>标签</span>
      </template>
      <el-menu-item v-for="tag in tagStore.tags" :key="tag.id"
        :index="`/notes?tagId=${tag.id}`">
        <el-tag :color="tag.color" size="small" style="color:#fff;">{{ tag.name }}</el-tag>
      </el-menu-item>
    </el-sub-menu>

    <el-menu-item index="/tags">
      <el-icon><Setting /></el-icon>
      <span>管理标签</span>
    </el-menu-item>
  </el-menu>

  <div style="padding:12px 16px; border-top:1px solid #e4e7ed;">
    <el-button @click="prefStore.toggleTheme()" size="small" style="width:100%;">
      <el-icon><component :is="prefStore.theme==='dark'?'Sunny':'Moon'" /></el-icon>
      {{ prefStore.theme === 'dark' ? '浅色模式' : '深色模式' }}
    </el-button>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useNotebookStore } from '../stores/notebook'
import { useTagStore } from '../stores/tag'
import { usePreferenceStore } from '../stores/preference'

export default {
  name: 'Sidebar',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const notebookStore = useNotebookStore()
    const tagStore = useTagStore()
    const prefStore = usePreferenceStore()
    const searchKeyword = ref(route.query.keyword || '')

    const activeMenu = computed(() => {
      if (route.path.startsWith('/tags')) return '/tags'
      if (route.path.startsWith('/search')) return ''
      return '/notes'
    })

    const goSearch = () => {
      if (searchKeyword.value) {
        router.push({ name: 'SearchView', query: { keyword: searchKeyword.value } })
      }
    }

    onMounted(async () => {
      await Promise.all([
        notebookStore.fetchNotebooks({ size: 50 }),
        tagStore.fetchTags(),
        prefStore.fetch()
      ])
      prefStore.applyTheme()
    })

    return { notebookStore, tagStore, prefStore, searchKeyword, activeMenu, goSearch }
  }
}
</script>
```

---

### Task 14: NotebookView (Main Note List View)

**Files:**
- Create: `frontend/src/views/NotebookView.vue`
- Create: `frontend/src/components/NoteList.vue`
- Create: `frontend/src/components/NotebookDialog.vue`

- [ ] **Step 1: Create NotebookView.vue**

```vue
<template>
  <div style="height:100%; display:flex; flex-direction:column; padding:20px;">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
      <div>
        <el-button type="primary" @click="showNotebookDialog = true">
          <el-icon><Plus /></el-icon> 新建笔记本
        </el-button>
        <el-button @click="goNewNote" type="success">
          <el-icon><Edit /></el-icon> 新建笔记
        </el-button>
      </div>
      <el-radio-group v-model="sort" size="small" @change="loadNotes">
        <el-radio-button value="updatedAt,desc">最近更新</el-radio-button>
        <el-radio-button value="createdAt,desc">最近创建</el-radio-button>
        <el-radio-button value="title,asc">标题排序</el-radio-button>
      </el-radio-group>
    </div>

    <NoteList :notebook-id="notebookId" :tag-id="tagId" :sort="sort" ref="noteListRef" />

    <NotebookDialog v-model:visible="showNotebookDialog" @saved="onNotebookSaved" />
  </div>
</template>

<script>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NoteList from '../components/NoteList.vue'
import NotebookDialog from '../components/NotebookDialog.vue'

export default {
  name: 'NotebookView',
  components: { NoteList, NotebookDialog },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const showNotebookDialog = ref(false)
    const sort = ref('updatedAt,desc')
    const notebookId = ref(route.query.notebookId || '')
    const tagId = ref(route.query.tagId || '')
    const noteListRef = ref(null)

    watch(() => route.query, (q) => {
      notebookId.value = q.notebookId || ''
      tagId.value = q.tagId || ''
    })

    const goNewNote = () => router.push('/notes/new')
    const loadNotes = () => noteListRef.value?.load()
    const onNotebookSaved = () => noteListRef.value?.load()

    return { showNotebookDialog, sort, notebookId, tagId, noteListRef, goNewNote, loadNotes, onNotebookSaved }
  }
}
</script>
```

- [ ] **Step 2: Create NoteList.vue**

```vue
<template>
  <div style="flex:1; overflow-y:auto;">
    <el-table :data="noteStore.notes" stripe v-loading="noteStore.loading"
      @row-click="(row) => router.push(`/notes/${row.id}/edit`)"
      style="width:100%; cursor:pointer;" @selection-change="onSelectionChange">
      <el-table-column type="selection" width="40" />
      <el-table-column prop="title" label="标题" min-width="200">
        <template #default="{ row }">
          <span v-html="row.title"></span>
        </template>
      </el-table-column>
      <el-table-column prop="notebookName" label="笔记本" width="120" />
      <el-table-column label="标签" width="180">
        <template #default="{ row }">
          <el-tag v-for="tag in row.tags" :key="tag.id" :color="tag.color"
            size="small" style="color:#fff; margin-right:4px;">{{ tag.name }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="160">
        <template #default="{ row }">{{ formatDate(row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click.stop="deleteNote(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="selectedIds.length" style="padding:8px 0;">
      <el-button type="danger" size="small" @click="batchDelete">
        批量删除 ({{ selectedIds.length }})
      </el-button>
    </div>

    <el-pagination v-if="noteStore.total > 0" style="margin-top:16px; justify-content:center;"
      :current-page="page" :page-size="size" :total="noteStore.total"
      layout="prev, pager, next" @current-change="onPageChange" />
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useNoteStore } from '../stores/note'

export default {
  name: 'NoteList',
  props: { notebookId: String, tagId: String, sort: { type: String, default: 'updatedAt,desc' } },
  setup(props) {
    const router = useRouter()
    const noteStore = useNoteStore()
    const page = ref(1)
    const size = 20
    const selectedIds = ref([])

    const load = () => {
      noteStore.fetchNotes({
        notebookId: props.notebookId || undefined,
        tagId: props.tagId || undefined,
        page: page.value, size, sort: props.sort
      })
    }

    const onSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }
    const onPageChange = (p) => { page.value = p; load() }

    const deleteNote = async (id) => {
      try {
        await ElMessageBox.confirm('确定要删除这篇笔记吗？', '确认删除', { type: 'warning' })
        await noteStore.remove(id)
        load()
      } catch (e) { /* cancelled */ }
    }

    const batchDelete = async () => {
      try {
        await ElMessageBox.confirm(`确定要删除 ${selectedIds.value.length} 篇笔记吗？`, '确认批量删除', { type: 'warning' })
        await noteStore.batchRemove(selectedIds.value)
        selectedIds.value = []
        load()
      } catch (e) { /* cancelled */ }
    }

    const formatDate = (d) => d ? new Date(d).toLocaleString('zh-CN') : ''

    watch(() => [props.notebookId, props.tagId, props.sort], () => { page.value = 1; load() })
    onMounted(load)

    return { noteStore, router, page, size, selectedIds, load, onSelectionChange, onPageChange, deleteNote, batchDelete, formatDate }
  }
}
</script>
```

- [ ] **Step 3: Create NotebookDialog.vue**

```vue
<template>
  <el-dialog :model-value="visible" @update:model-value="$emit('update:visible', $event)"
    title="新建笔记本" width="450px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="名称" required>
        <el-input v-model="form.name" placeholder="笔记本名称" maxlength="100" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" placeholder="可选描述" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="submit" :disabled="!form.name.trim()">创建</el-button>
    </template>
  </el-dialog>
</template>

<script>
import { reactive } from 'vue'
import { useNotebookStore } from '../stores/notebook'

export default {
  name: 'NotebookDialog',
  props: { visible: Boolean },
  emits: ['update:visible', 'saved'],
  setup(props, { emit }) {
    const notebookStore = useNotebookStore()
    const form = reactive({ name: '', description: '' })

    const submit = async () => {
      await notebookStore.create({ name: form.name.trim(), description: form.description.trim() })
      form.name = ''; form.description = ''
      emit('update:visible', false)
      emit('saved')
    }

    return { form, submit }
  }
}
</script>
```

---

### Task 15: NoteEditor View

**Files:**
- Create: `frontend/src/views/NoteEditor.vue`
- Create: `frontend/src/components/MarkdownEditor.vue`
- Create: `frontend/src/components/MarkdownPreview.vue`

- [ ] **Step 1: Create NoteEditor.vue**

```vue
<template>
  <div style="height:100%; display:flex; flex-direction:column;">
    <div style="padding:12px 20px; border-bottom:1px solid #e4e7ed; display:flex; align-items:center; gap:12px;">
      <el-input v-model="title" placeholder="笔记标题" style="width:300px;" maxlength="200" />
      <el-select v-model="notebookId" placeholder="选择笔记本" style="width:180px;">
        <el-option v-for="nb in notebookStore.notebooks" :key="nb.id"
          :label="nb.name" :value="nb.id" />
      </el-select>
      <el-select v-model="selectedTagIds" placeholder="选择标签" style="width:200px;" multiple>
        <el-option v-for="tag in tagStore.tags" :key="tag.id" :label="tag.name" :value="tag.id" />
      </el-select>
      <el-button @click="uploadRef.click()" size="small">
        <el-icon><Upload /></el-icon> 插入附件
      </el-button>
      <input ref="uploadRef" type="file" hidden @change="onFileUpload" />
      <div style="flex:1;" />
      <el-button @click="save" type="primary" :disabled="!canSave">保存</el-button>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <div style="flex:1; display:flex; overflow:hidden;">
      <MarkdownEditor v-model="content" :font-size="prefStore.editorFontSize"
        style="flex:1; border-right:1px solid #e4e7ed;" />
      <MarkdownPreview :content="content" style="flex:1;" />
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useNoteStore } from '../stores/note'
import { useNotebookStore } from '../stores/notebook'
import { useTagStore } from '../stores/tag'
import { usePreferenceStore } from '../stores/preference'
import { useDraftStore } from '../stores/draft'
import { attachmentApi } from '../api/attachments'
import MarkdownEditor from '../components/MarkdownEditor.vue'
import MarkdownPreview from '../components/MarkdownPreview.vue'

export default {
  name: 'NoteEditor',
  components: { MarkdownEditor, MarkdownPreview },
  props: { id: String },
  setup(props) {
    const route = useRoute()
    const router = useRouter()
    const noteStore = useNoteStore()
    const notebookStore = useNotebookStore()
    const tagStore = useTagStore()
    const prefStore = usePreferenceStore()
    const draftStore = useDraftStore()

    const noteId = ref(props.id || null)
    const title = ref('')
    const content = ref('')
    const notebookId = ref('')
    const selectedTagIds = ref([])
    const uploadRef = ref(null)
    let autoSaveTimer = null

    const canSave = computed(() => title.value.trim() && notebookId.value)

    const save = async () => {
      const data = {
        title: title.value.trim(),
        content: content.value,
        notebookId: notebookId.value,
        tagIds: selectedTagIds.value
      }
      if (noteId.value) {
        await noteStore.update(noteId.value, data)
      } else {
        const result = await noteStore.create(data)
        noteId.value = result.id
        router.replace(`/notes/${result.id}/edit`)
      }
      ElMessage.success('保存成功')
      draftStore.clear()
    }

    const onFileUpload = async (e) => {
      const file = e.target.files[0]
      if (!file || !noteId.value) {
        if (!noteId.value) ElMessage.warning('请先保存笔记后再上传附件')
        return
      }
      const fd = new FormData()
      fd.append('file', file)
      fd.append('noteId', noteId.value)
      try {
        const res = await attachmentApi.upload(fd)
        content.value += '\n' + res.data.markdownRef
      } catch (e) { /* handled by interceptor */ }
      uploadRef.value.value = ''
    }

    const startAutoSave = () => {
      autoSaveTimer = setInterval(() => {
        if (title.value.trim()) {
          draftStore.save({
            noteId: noteId.value,
            title: title.value,
            content: content.value,
            notebookId: notebookId.value
          })
        }
      }, prefStore.autoSaveInterval * 1000)
    }

    onMounted(async () => {
      await Promise.all([
        notebookStore.fetchNotebooks({ size: 50 }),
        tagStore.fetchTags()
      ])

      if (noteId.value) {
        const note = await noteStore.fetchNote(noteId.value)
        title.value = note.title
        content.value = note.content || ''
        notebookId.value = note.notebookId
        selectedTagIds.value = note.tags.map(t => t.id)
      } else {
        const draft = await draftStore.fetchLatest()
        if (draft) {
          title.value = draft.title || ''
          content.value = draft.content || ''
          notebookId.value = draft.notebookId || ''
        }
      }
      startAutoSave()
    })

    onUnmounted(() => { clearInterval(autoSaveTimer) })

    return {
      title, content, notebookId, selectedTagIds, uploadRef, canSave, save, onFileUpload,
      router, notebookStore, tagStore, prefStore, noteStore
    }
  }
}
</script>
```

- [ ] **Step 2: Create MarkdownEditor.vue**

```vue
<template>
  <div style="height:100%; overflow:hidden;">
    <textarea ref="editorRef" :value="modelValue" @input="onInput"
      style="width:100%; height:100%; border:none; padding:16px; resize:none; outline:none;
             font-family:'Fira Code', monospace; line-height:1.6;"
      :style="{ fontSize: fontSize + 'px' }"
      placeholder="使用 Markdown 语法编写笔记..."></textarea>
  </div>
</template>

<script>
export default {
  name: 'MarkdownEditor',
  props: { modelValue: String, fontSize: { type: Number, default: 14 } },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const onInput = (e) => emit('update:modelValue', e.target.value)
    return { onInput }
  }
}
</script>
```

- [ ] **Step 3: Create MarkdownPreview.vue**

```vue
<template>
  <div style="height:100%; overflow-y:auto; padding:16px;"
    v-html="rendered" class="markdown-body"></div>
</template>

<script>
import { computed } from 'vue'
import { marked } from 'marked'

marked.setOptions({ breaks: true, gfm: true })

export default {
  name: 'MarkdownPreview',
  props: { content: String },
  setup(props) {
    const rendered = computed(() => marked(props.content || ''))
    return { rendered }
  }
}
</script>

<style>
.markdown-body { line-height:1.8; }
.markdown-body h1 { font-size:1.8em; border-bottom:2px solid #409EFF; padding-bottom:8px; margin:16px 0; }
.markdown-body h2 { font-size:1.4em; margin:14px 0; }
.markdown-body h3 { font-size:1.2em; margin:12px 0; }
.markdown-body pre { background:#f5f7fa; padding:12px; border-radius:6px; overflow-x:auto; }
.markdown-body code { background:#f0f2f5; padding:2px 6px; border-radius:3px; font-size:0.9em; }
.markdown-body pre code { background:none; padding:0; }
.markdown-body img { max-width:100%; border-radius:4px; }
.markdown-body blockquote { border-left:4px solid #409EFF; padding-left:16px; color:#666; margin:12px 0; }
.markdown-body table { border-collapse:collapse; width:100%; }
.markdown-body th, .markdown-body td { border:1px solid #ddd; padding:8px 12px; }
.markdown-body th { background:#f5f7fa; }
.markdown-body em { background:#fff3cd; padding:0 2px; font-style:normal; }
</style>
```

---

### Task 16: TagManager & SearchView

**Files:**
- Create: `frontend/src/views/TagManager.vue`
- Create: `frontend/src/components/TagDialog.vue`
- Create: `frontend/src/views/SearchView.vue`

- [ ] **Step 1: Create TagManager.vue**

```vue
<template>
  <div style="padding:20px;">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
      <h3>标签管理</h3>
      <el-button type="primary" @click="dialogVisible = true; editingTag = null;">
        <el-icon><Plus /></el-icon> 新建标签
      </el-button>
    </div>

    <div v-if="tagStore.tags.length === 0" style="text-align:center; color:#909399; padding:60px;">
      暂无标签，点击上方按钮创建
    </div>

    <el-table :data="tagStore.tags" v-loading="tagStore.loading" style="width:100%;">
      <el-table-column label="颜色" width="80">
        <template #default="{ row }">
          <div :style="{ width:'24px', height:'24px', background:row.color, borderRadius:'4px' }"></div>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="noteCount" label="关联笔记数" width="120" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="editTag(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteTag(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <TagDialog v-model:visible="dialogVisible" :editing="editingTag" @saved="tagStore.fetchTags()" />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useTagStore } from '../stores/tag'
import TagDialog from '../components/TagDialog.vue'

export default {
  name: 'TagManager',
  components: { TagDialog },
  setup() {
    const tagStore = useTagStore()
    const dialogVisible = ref(false)
    const editingTag = ref(null)

    const editTag = (tag) => { editingTag.value = tag; dialogVisible.value = true }
    const deleteTag = async (id) => {
      try {
        await ElMessageBox.confirm('确定要删除这个标签吗？将解除与所有笔记的关联。', '确认删除', { type: 'warning' })
        await tagStore.remove(id)
      } catch (e) { /* cancelled */ }
    }

    onMounted(() => tagStore.fetchTags())

    return { tagStore, dialogVisible, editingTag, editTag, deleteTag }
  }
}
</script>
```

- [ ] **Step 2: Create TagDialog.vue**

```vue
<template>
  <el-dialog :model-value="visible" @update:model-value="$emit('update:visible', $event)"
    :title="editing ? '编辑标签' : '新建标签'" width="400px">
    <el-form :model="form" label-width="60px">
      <el-form-item label="名称" required>
        <el-input v-model="form.name" maxlength="30" placeholder="标签名称" />
      </el-form-item>
      <el-form-item label="颜色">
        <el-color-picker v-model="form.color" />
        <span style="margin-left:8px; color:#909399;">{{ form.color }}</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="submit" :disabled="!form.name.trim()">
        {{ editing ? '保存' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script>
import { reactive, watch } from 'vue'
import { useTagStore } from '../stores/tag'

export default {
  name: 'TagDialog',
  props: { visible: Boolean, editing: Object },
  emits: ['update:visible', 'saved'],
  setup(props, { emit }) {
    const tagStore = useTagStore()
    const form = reactive({ name: '', color: '#409EFF' })

    watch(() => props.editing, (tag) => {
      if (tag) { form.name = tag.name; form.color = tag.color }
      else { form.name = ''; form.color = '#409EFF' }
    })

    const submit = async () => {
      if (props.editing) {
        await tagStore.update(props.editing.id, { name: form.name.trim(), color: form.color })
      } else {
        await tagStore.create({ name: form.name.trim(), color: form.color })
      }
      emit('update:visible', false)
      emit('saved')
    }

    return { form, submit }
  }
}
</script>
```

- [ ] **Step 3: Create SearchView.vue**

```vue
<template>
  <div style="height:100%; display:flex; flex-direction:column; padding:20px;">
    <div style="margin-bottom:16px;">
      <el-input v-model="keyword" placeholder="输入关键词搜索..." clearable
        @keyup.enter="search" @clear="search" style="max-width:400px;">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="filterNotebookId" placeholder="限定笔记本" clearable style="width:180px; margin-left:8px;">
        <el-option v-for="nb in notebookStore.notebooks" :key="nb.id" :label="nb.name" :value="nb.id" />
      </el-select>
      <el-select v-model="filterTagId" placeholder="限定标签" clearable style="width:180px; margin-left:8px;">
        <el-option v-for="tag in tagStore.tags" :key="tag.id" :label="tag.name" :value="tag.id" />
      </el-select>
      <el-button type="primary" @click="search" style="margin-left:8px;">搜索</el-button>
    </div>

    <div v-if="results.length === 0 && searched" style="text-align:center; color:#909399; padding:60px;">
      未找到匹配的笔记
    </div>

    <div v-for="note in results" :key="note.id" style="margin-bottom:16px; cursor:pointer;"
      @click="router.push(`/notes/${note.id}/edit`)">
      <el-card shadow="hover">
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <h4 v-html="note.title" style="margin:0;"></h4>
          <span style="color:#909399; font-size:12px;">{{ note.notebookName }}</span>
        </div>
        <p v-html="note.plainText" style="margin:8px 0 0; color:#666; font-size:14px;"></p>
        <div style="margin-top:8px;">
          <el-tag v-for="tag in note.tags" :key="tag.id" :color="tag.color"
            size="small" style="color:#fff; margin-right:4px;">{{ tag.name }}</el-tag>
          <span style="color:#909399; font-size:12px; margin-left:8px;">
            {{ new Date(note.updatedAt).toLocaleString('zh-CN') }}
          </span>
        </div>
      </el-card>
    </div>

    <el-pagination v-if="total > 0" style="margin-top:auto; justify-content:center;"
      :current-page="page" :page-size="size" :total="total"
      layout="prev, pager, next" @current-change="p => { page = p; search() }" />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchApi } from '../api/search'
import { useNotebookStore } from '../stores/notebook'
import { useTagStore } from '../stores/tag'

export default {
  name: 'SearchView',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const notebookStore = useNotebookStore()
    const tagStore = useTagStore()

    const keyword = ref(route.query.keyword || '')
    const filterNotebookId = ref('')
    const filterTagId = ref('')
    const results = ref([])
    const total = ref(0)
    const page = ref(1)
    const size = 20
    const searched = ref(false)

    const search = async () => {
      if (!keyword.value.trim()) return
      searched.value = true
      try {
        const res = await searchApi.search({
          keyword: keyword.value.trim(),
          notebookId: filterNotebookId.value || undefined,
          tagId: filterTagId.value || undefined,
          page: page.value, size
        })
        results.value = res.data.content || []
        total.value = res.data.totalElements || 0
      } catch (e) { /* handled */ }
    }

    onMounted(async () => {
      await Promise.all([
        notebookStore.fetchNotebooks({ size: 50 }),
        tagStore.fetchTags()
      ])
      if (keyword.value) search()
    })

    return { keyword, filterNotebookId, filterTagId, results, total, page, size, searched, search, router, notebookStore, tagStore }
  }
}
</script>
```

---

## Self-Review

### 1. Spec Coverage Check

| Requirement | Task(s) |
|---|---|
| 2.1 Notebook CRUD | Task 6 (NotebookService), Task 7 (NotebookController) |
| 2.2 Note CRUD + batch delete | Task 6 (NoteService), Task 7 (NoteController) |
| 2.3 Tag CRUD + note association | Task 6 (TagService), Task 7 (TagController), Task 8 (fix) |
| 2.4 Full-text search + highlight | Task 6 (SearchService), Task 7 (SearchController) |
| 2.5 Attachment upload/preview/cleanup | Task 6 (AttachmentService), Task 7 (AttachmentController) |
| 2.6 Export, theme, auto-save | Task 6 (ExportService, DraftService, PreferenceService), Task 7 |
| 3. Non-functional (pagination, transaction, CORS) | Task 1 (WebConfig, max page size 50), Task 6 (@Transactional), Task 5 (error handling) |

### 2. Placeholder Scan

No TBD, TODO, or placeholder patterns found. All steps contain complete code.

### 3. Type Consistency

- All DTO field names match API documentation response fields
- Entity relationships match the ER diagram in API docs
- Frontend API calls match backend controller paths
- Store actions align with API methods

---

**Plan complete and saved to `docs/superpowers/plans/2026-05-19-classnote-local.md`. Two execution options:**

1. **Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

2. **Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
