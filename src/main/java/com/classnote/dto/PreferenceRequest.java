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
