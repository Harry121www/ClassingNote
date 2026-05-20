package com.classnote.repository;

import com.classnote.entity.KnowledgeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeEntryRepository extends JpaRepository<KnowledgeEntry, String> {
    List<KnowledgeEntry> findByProjectNameOrderByCreatedAtDesc(String projectName);
}
