package com.classnote.dto;

import com.classnote.entity.Attachment;

public class AttachmentResponse {
    private String id;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String mimeType;

    public static AttachmentResponse from(Attachment att) {
        AttachmentResponse r = new AttachmentResponse();
        r.id = att.getId();
        r.fileName = att.getFileName();
        r.filePath = att.getFilePath();
        r.fileSize = att.getFileSize();
        r.mimeType = att.getMimeType();
        return r;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
}
