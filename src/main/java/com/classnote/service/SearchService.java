package com.classnote.service;

import com.classnote.dto.NoteListResponse;
import com.classnote.entity.Note;
import com.classnote.repository.NoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class SearchService {

    private final NoteRepository noteRepo;

    public SearchService(NoteRepository noteRepo) {
        this.noteRepo = noteRepo;
    }

    public Page<NoteListResponse> search(String keyword, String notebookId, String tagId,
                                          int page, int size) {
        Page<Note> notes = noteRepo.search(notebookId, tagId,
                keyword != null ? keyword.trim() : null,
                PageRequest.of(page, size));
        return notes.map(note -> {
            NoteListResponse r = NoteListResponse.from(note);
            if (keyword != null && !keyword.isEmpty()) {
                String kw = keyword.trim();
                r.setTitle(highlight(r.getTitle(), kw));
                r.setPlainText(highlight(r.getPlainText(), kw));
            }
            return r;
        });
    }

    private String highlight(String text, String keyword) {
        if (text == null || keyword == null) return text;
        return text.replaceAll("(?i)(" + Pattern.quote(keyword) + ")", "<em>$1</em>");
    }
}
