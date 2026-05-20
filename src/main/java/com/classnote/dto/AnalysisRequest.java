package com.classnote.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

public class AnalysisRequest {
    @NotEmpty
    @Size(max = 100)
    private List<FileItem> files;
    private String projectName;
    private String mode = "ARCHITECTURE";

    public List<FileItem> getFiles() { return files; }
    public void setFiles(List<FileItem> files) { this.files = files; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}
