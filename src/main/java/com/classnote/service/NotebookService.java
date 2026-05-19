package com.classnote.service;

import com.classnote.dto.NotebookRequest;
import com.classnote.entity.Notebook;
import com.classnote.exception.ResourceNotFoundException;
import com.classnote.repository.NotebookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotebookService {

    private final NotebookRepository notebookRepo;

    public NotebookService(NotebookRepository notebookRepo) {
        this.notebookRepo = notebookRepo;
    }

    public Notebook create(NotebookRequest req) {
        Notebook nb = new Notebook();
        nb.setName(req.getName());
        nb.setDescription(req.getDescription());
        return notebookRepo.save(nb);
    }

    public Page<Notebook> list(int page, int size, String sort) {
        String[] parts = sort.split(",");
        Sort.Direction dir = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return notebookRepo.findAll(PageRequest.of(page, size, Sort.by(dir, parts[0])));
    }

    public Notebook getById(String id) {
        return notebookRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notebook", id));
    }

    public Notebook update(String id, NotebookRequest req) {
        Notebook nb = getById(id);
        if (req.getName() != null) nb.setName(req.getName());
        if (req.getDescription() != null) nb.setDescription(req.getDescription());
        return notebookRepo.save(nb);
    }

    @Transactional
    public int delete(String id) {
        Notebook nb = getById(id);
        int noteCount = nb.getNotes().size();
        notebookRepo.delete(nb);
        return noteCount;
    }
}
