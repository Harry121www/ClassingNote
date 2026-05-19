package com.classnote.dto;

public class ExportResponse {
    private String exportPath;
    private String exportDir;
    private int notesExported;
    private int attachmentsCopied;

    public static ExportResponse forNote(String path, int copied) {
        ExportResponse r = new ExportResponse();
        r.exportPath = path;
        r.attachmentsCopied = copied;
        return r;
    }

    public static ExportResponse forNotebook(String dir, int notes, int copied) {
        ExportResponse r = new ExportResponse();
        r.exportDir = dir;
        r.notesExported = notes;
        r.attachmentsCopied = copied;
        return r;
    }

    public String getExportPath() { return exportPath; }
    public void setExportPath(String exportPath) { this.exportPath = exportPath; }
    public String getExportDir() { return exportDir; }
    public void setExportDir(String exportDir) { this.exportDir = exportDir; }
    public int getNotesExported() { return notesExported; }
    public void setNotesExported(int notesExported) { this.notesExported = notesExported; }
    public int getAttachmentsCopied() { return attachmentsCopied; }
    public void setAttachmentsCopied(int attachmentsCopied) { this.attachmentsCopied = attachmentsCopied; }
}
