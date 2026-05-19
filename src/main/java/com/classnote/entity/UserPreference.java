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
