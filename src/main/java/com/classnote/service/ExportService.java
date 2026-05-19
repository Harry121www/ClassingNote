package com.classnote.service;

import com.classnote.dto.ExportResponse;
import com.classnote.entity.Attachment;
import com.classnote.entity.Note;
import com.classnote.entity.Notebook;
import com.classnote.exception.ResourceNotFoundException;
import com.classnote.repository.NotebookRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class ExportService {

    private final NotebookRepository notebookRepo;
    private final NoteService noteService;

    public ExportService(NotebookRepository notebookRepo, NoteService noteService) {
        this.notebookRepo = notebookRepo;
        this.noteService = noteService;
    }

    public ExportResponse exportNote(String noteId, String targetDir) throws IOException {
        Note note = noteService.getById(noteId);
        Path dir = Paths.get(targetDir);
        Files.createDirectories(dir);

        String safeName = sanitizeFileName(note.getTitle()) + ".md";
        Path mdFile = dir.resolve(safeName);
        Files.writeString(mdFile, note.getContent() != null ? note.getContent() : "");

        int copied = copyAttachments(note, dir.resolve("attachments"));
        return ExportResponse.forNote(mdFile.toString(), copied);
    }

    public ExportResponse exportNotebook(String notebookId, String targetDir) throws IOException {
        Notebook nb = notebookRepo.findById(notebookId)
                .orElseThrow(() -> new ResourceNotFoundException("Notebook", notebookId));

        Path nbDir = Paths.get(targetDir, sanitizeFileName(nb.getName()));
        Files.createDirectories(nbDir);

        int totalCopied = 0;
        int exported = 0;
        for (Note note : nb.getNotes()) {
            String safeName = sanitizeFileName(note.getTitle()) + ".md";
            Files.writeString(nbDir.resolve(safeName),
                    note.getContent() != null ? note.getContent() : "");
            totalCopied += copyAttachments(note, nbDir.resolve("attachments"));
            exported++;
        }
        return ExportResponse.forNotebook(nbDir.toString(), exported, totalCopied);
    }

    private int copyAttachments(Note note, Path attachmentsDir) throws IOException {
        int count = 0;
        for (Attachment att : note.getAttachments()) {
            Path src = Paths.get(att.getFilePath());
            if (Files.exists(src)) {
                Files.createDirectories(attachmentsDir);
                Files.copy(src, attachmentsDir.resolve(att.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                count++;
            }
        }
        return count;
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }
}
