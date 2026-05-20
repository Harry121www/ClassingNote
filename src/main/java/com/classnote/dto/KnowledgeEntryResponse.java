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
