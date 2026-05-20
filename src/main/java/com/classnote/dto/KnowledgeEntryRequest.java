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
