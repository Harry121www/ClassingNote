package com.classnote.service;

import com.classnote.dto.DraftRequest;
import com.classnote.entity.Draft;
import com.classnote.repository.DraftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DraftService {

    private final DraftRepository draftRepo;

    public DraftService(DraftRepository draftRepo) {
        this.draftRepo = draftRepo;
    }

    @Transactional
    public Draft save(DraftRequest req) {
        draftRepo.deleteAll();
        Draft draft = new Draft();
        draft.setNoteId(req.getNoteId());
        draft.setTitle(req.getTitle());
        draft.setContent(req.getContent());
        draft.setNotebookId(req.getNotebookId());
        return draftRepo.save(draft);
    }

    public Draft getLatest() {
        return draftRepo.findTopByOrderBySavedAtDesc();
    }

    @Transactional
    public void delete() {
        draftRepo.deleteAll();
    }
}
