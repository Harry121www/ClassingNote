package com.classnote.repository;

import com.classnote.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {

    boolean existsByName(String name);

    @Query("SELECT COUNT(n) FROM Note n JOIN n.tags t WHERE t.id = :tagId")
    long countNotesByTagId(@Param("tagId") String tagId);
}
