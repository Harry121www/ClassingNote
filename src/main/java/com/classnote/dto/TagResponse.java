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
