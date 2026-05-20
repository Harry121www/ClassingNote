package com.classnote.dto;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public class SummarizeRequest {
    @NotEmpty
    private List<String> noteIds;

    public List<String> getNoteIds() { return noteIds; }
    public void setNoteIds(List<String> noteIds) { this.noteIds = noteIds; }
}
