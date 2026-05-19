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
