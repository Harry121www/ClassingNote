package com.classnote.service;

import com.classnote.config.FileStorageConfig;
import com.classnote.entity.Attachment;
import com.classnote.entity.Note;
import com.classnote.exception.ResourceNotFoundException;
import com.classnote.repository.AttachmentRepository;
import com.classnote.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepo;
    private final NoteRepository noteRepo;
    private final FileStorageConfig fileStorageConfig;

    public AttachmentService(AttachmentRepository attachmentRepo,
                             NoteRepository noteRepo,
                             FileStorageConfig fileStorageConfig) {
        this.attachmentRepo = attachmentRepo;
        this.noteRepo = noteRepo;
        this.fileStorageConfig = fileStorageConfig;
    }

    @Transactional
    public Attachment upload(MultipartFile file, String noteId) throws IOException {
        Note note = noteRepo.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", noteId));

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        Path uploadDir = fileStorageConfig.getAttachmentRoot().resolve(datePath);
        Files.createDirectories(uploadDir);

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString() + ext;
        Path targetPath = uploadDir.resolve(storedName);
        file.transferTo(targetPath.toFile());

        Attachment att = new Attachment();
        att.setFileName(originalName);
        att.setFilePath(targetPath.toString());
        att.setFileSize(file.getSize());
        att.setMimeType(file.getContentType());
        att.setNote(note);
        return attachmentRepo.save(att);
    }

    public List<Attachment> listByNote(String noteId) {
        return attachmentRepo.findByNoteId(noteId);
    }

    @Transactional
    public void delete(String id) {
        Attachment att = attachmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", id));
        String filePath = att.getFilePath();
        String noteId = att.getNote().getId();
        attachmentRepo.delete(att);

        boolean stillReferenced = attachmentRepo.existsByFilePathAndNoteIdNot(filePath, noteId);
        if (!stillReferenced) {
            try {
                Files.deleteIfExists(Path.of(filePath));
            } catch (IOException ignored) {}
        }
    }
}
