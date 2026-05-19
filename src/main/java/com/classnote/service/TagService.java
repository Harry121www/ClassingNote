package com.classnote.service;

import com.classnote.dto.TagRequest;
import com.classnote.entity.Tag;
import com.classnote.exception.DuplicateResourceException;
import com.classnote.exception.ResourceNotFoundException;
import com.classnote.repository.TagRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepo;

    public TagService(TagRepository tagRepo) {
        this.tagRepo = tagRepo;
    }

    public Tag create(TagRequest req) {
        if (tagRepo.existsByName(req.getName())) {
            throw new DuplicateResourceException("Tag name already exists: " + req.getName());
        }
        Tag tag = new Tag();
        tag.setName(req.getName());
        tag.setColor(req.getColor());
        return tagRepo.save(tag);
    }

    public List<Tag> list(String sort) {
        String[] parts = sort.split(",");
        Sort.Direction dir = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return tagRepo.findAll(Sort.by(dir, parts[0]));
    }

    public Tag getById(String id) {
        return tagRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));
    }

    public long countNotesByTag(String tagId) {
        return tagRepo.countNotesByTagId(tagId);
    }

    @Transactional
    public Tag update(String id, TagRequest req) {
        Tag tag = getById(id);
        if (req.getName() != null) {
            if (!tag.getName().equals(req.getName()) && tagRepo.existsByName(req.getName())) {
                throw new DuplicateResourceException("Tag name already exists: " + req.getName());
            }
            tag.setName(req.getName());
        }
        if (req.getColor() != null) tag.setColor(req.getColor());
        return tagRepo.save(tag);
    }

    @Transactional
    public long delete(String id) {
        Tag tag = getById(id);
        long noteCount = tagRepo.countNotesByTagId(id);
        tagRepo.delete(tag);
        return noteCount;
    }
}
