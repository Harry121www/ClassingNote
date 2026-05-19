package com.classnote.service;

import com.classnote.dto.BatchDeleteRequest;
import com.classnote.dto.DeleteResult;
import com.classnote.dto.NoteRequest;
import com.classnote.entity.Attachment;
import com.classnote.entity.Note;
import com.classnote.entity.Notebook;
import com.classnote.entity.Tag;
import com.classnote.exception.ResourceNotFoundException;
import com.classnote.repository.AttachmentRepository;
import com.classnote.repository.NoteRepository;
import com.classnote.repository.NotebookRepository;
import com.classnote.repository.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepo;
    private final NotebookRepository notebookRepo;
    private final TagRepository tagRepo;
    private final AttachmentRepository attachmentRepo;

    public NoteService(NoteRepository noteRepo, NotebookRepository notebookRepo,
                       TagRepository tagRepo, AttachmentRepository attachmentRepo) {
        this.noteRepo = noteRepo;
        this.notebookRepo = notebookRepo;
        this.tagRepo = tagRepo;
        this.attachmentRepo = attachmentRepo;
    }

    @Transactional
    public Note create(NoteRequest req) {
        Notebook notebook = notebookRepo.findById(req.getNotebookId())
                .orElseThrow(() -> new ResourceNotFoundException("Notebook", req.getNotebookId()));
        Note note = new Note();
        note.setTitle(req.getTitle());
        note.setContent(req.getContent());
        note.setPlainText(stripMarkdown(req.getContent()));
        note.setNotebook(notebook);
        if (req.getTagIds() != null && !req.getTagIds().isEmpty()) {
            note.setTags(new HashSet<>(tagRepo.findAllById(req.getTagIds())));
        }
        return noteRepo.save(note);
    }

    public Page<Note> list(String notebookId, String tagId, String keyword,
                           int page, int size, String sort) {
        String[] parts = sort.split(",");
        Sort.Direction dir = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = parts[0].equals("title") ? "title" :
                          parts[0].equals("createdAt") ? "createdAt" : "updatedAt";
        return noteRepo.search(notebookId, tagId, keyword,
                PageRequest.of(page, size, Sort.by(dir, sortField)));
    }

    public Note getById(String id) {
        Note note = noteRepo.findByIdWithTags(id);
        if (note == null) throw new ResourceNotFoundException("Note", id);
        note.getAttachments().size();
        return note;
    }

    @Transactional
    public Note update(String id, NoteRequest req) {
        Note note = getById(id);
        if (req.getNotebookId() != null) {
            Notebook nb = notebookRepo.findById(req.getNotebookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Notebook", req.getNotebookId()));
            note.setNotebook(nb);
        }
        if (req.getTitle() != null) note.setTitle(req.getTitle());
        if (req.getContent() != null) {
            note.setContent(req.getContent());
            note.setPlainText(stripMarkdown(req.getContent()));
        }
        if (req.getTagIds() != null) {
            note.setTags(new HashSet<>(tagRepo.findAllById(req.getTagIds())));
        }
        return noteRepo.save(note);
    }

    @Transactional
    public void delete(String id) {
        Note note = getById(id);
        cleanupAttachments(note);
        noteRepo.delete(note);
    }

    @Transactional
    public DeleteResult batchDelete(BatchDeleteRequest req) {
        List<Note> notes = noteRepo.findAllById(req.getIds());
        int cleanedAttachments = 0;
        for (Note note : notes) {
            cleanedAttachments += cleanupAttachments(note);
        }
        noteRepo.deleteAllInBatch(notes);
        return new DeleteResult(notes.size(), cleanedAttachments);
    }

    private int cleanupAttachments(Note note) {
        int cleaned = 0;
        for (Attachment att : note.getAttachments()) {
            boolean referenced = attachmentRepo.existsByFilePathAndNoteIdNot(att.getFilePath(), note.getId());
            if (!referenced) {
                try {
                    Files.deleteIfExists(Paths.get(att.getFilePath()));
                    cleaned++;
                } catch (IOException ignored) {}
            }
        }
        return cleaned;
    }

    private String stripMarkdown(String md) {
        if (md == null) return "";
        return md.replaceAll("#+\\s", "")
                 .replaceAll("\\*\\*?(.*?)\\*\\*?", "$1")
                 .replaceAll("!\\[.*?\\]\\(.*?\\)", "")
                 .replaceAll("\\[(.*?)\\]\\(.*?\\)", "$1")
                 .replaceAll("`{1,3}.*?`{1,3}", "")
                 .replaceAll("[-*+]\\s", "")
                 .replaceAll("\\n+", " ")
                 .trim();
    }
}
