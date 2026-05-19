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
