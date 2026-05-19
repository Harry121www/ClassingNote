package com.classnote.repository;

import com.classnote.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<Note, String> {

    Page<Note> findByNotebookId(String notebookId, Pageable pageable);

    @Query("SELECT n FROM Note n LEFT JOIN FETCH n.tags WHERE n.id = :id")
    Note findByIdWithTags(@Param("id") String id);

    @Query("SELECT DISTINCT n FROM Note n LEFT JOIN n.tags t WHERE " +
           "(:notebookId IS NULL OR n.notebook.id = :notebookId) AND " +
           "(:tagId IS NULL OR t.id = :tagId) AND " +
           "(:keyword IS NULL OR n.title LIKE %:keyword% OR n.plainText LIKE %:keyword%)")
    Page<Note> search(@Param("notebookId") String notebookId,
                      @Param("tagId") String tagId,
                      @Param("keyword") String keyword,
                      Pageable pageable);

    long countByNotebookId(String notebookId);
}
