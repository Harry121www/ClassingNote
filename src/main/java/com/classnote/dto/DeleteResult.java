package com.classnote.dto;

public class DeleteResult {
    private int deletedCount;
    private int cleanedAttachments;

    public DeleteResult(int deletedCount, int cleanedAttachments) {
        this.deletedCount = deletedCount;
        this.cleanedAttachments = cleanedAttachments;
    }

    public int getDeletedCount() { return deletedCount; }
    public void setDeletedCount(int deletedCount) { this.deletedCount = deletedCount; }
    public int getCleanedAttachments() { return cleanedAttachments; }
    public void setCleanedAttachments(int cleanedAttachments) { this.cleanedAttachments = cleanedAttachments; }
}
