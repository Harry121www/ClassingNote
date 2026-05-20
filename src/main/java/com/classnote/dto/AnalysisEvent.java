package com.classnote.dto;

public class AnalysisEvent {
    private String phase;
    private String path;
    private String content;
    private boolean complete;

    public AnalysisEvent() {}

    public AnalysisEvent(String phase, String path, String content, boolean complete) {
        this.phase = phase;
        this.path = path;
        this.content = content;
        this.complete = complete;
    }

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isComplete() { return complete; }
    public void setComplete(boolean complete) { this.complete = complete; }
}
