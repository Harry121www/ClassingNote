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
