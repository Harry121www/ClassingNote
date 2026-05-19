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
