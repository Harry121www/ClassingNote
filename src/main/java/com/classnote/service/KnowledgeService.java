package com.classnote.service;

import com.classnote.dto.KnowledgeEntryRequest;
import com.classnote.entity.KnowledgeEntry;
import com.classnote.exception.ResourceNotFoundException;
import com.classnote.repository.KnowledgeEntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeService {

    private final KnowledgeEntryRepository repo;

    public KnowledgeService(KnowledgeEntryRepository repo) {
        this.repo = repo;
    }

    public KnowledgeEntry create(KnowledgeEntryRequest req) {
        KnowledgeEntry entry = new KnowledgeEntry();
        entry.setTitle(req.getTitle());
        entry.setContent(req.getContent());
        entry.setProjectName(req.getProjectName());
        entry.setCategory(req.getCategory());
        entry.setFilePath(req.getFilePath());
        entry.setPackageName(req.getPackageName());
        entry.setSourceNoteId(req.getSourceNoteId());
        return repo.save(entry);
    }

    public List<KnowledgeEntry> listByProject(String projectName) {
        return repo.findByProjectNameOrderByCreatedAtDesc(projectName);
    }

    public KnowledgeEntry getById(String id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("KnowledgeEntry", id));
    }

    public KnowledgeEntry update(String id, KnowledgeEntryRequest req) {
        KnowledgeEntry entry = getById(id);
        if (req.getTitle() != null) entry.setTitle(req.getTitle());
        if (req.getContent() != null) entry.setContent(req.getContent());
        if (req.getProjectName() != null) entry.setProjectName(req.getProjectName());
        if (req.getCategory() != null) entry.setCategory(req.getCategory());
        entry.setFilePath(req.getFilePath());
        entry.setPackageName(req.getPackageName());
        if (req.getSourceNoteId() != null) entry.setSourceNoteId(req.getSourceNoteId());
        return repo.save(entry);
    }

    public void delete(String id) {
        KnowledgeEntry entry = getById(id);
        repo.delete(entry);
    }
}
