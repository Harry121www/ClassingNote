package com.classnote.repository;

import com.classnote.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, String> {

    List<Attachment> findByNoteId(String noteId);

    @Query("SELECT COUNT(a) > 0 FROM Attachment a WHERE a.filePath = :filePath AND a.note.id <> :noteId")
    boolean existsByFilePathAndNoteIdNot(@Param("filePath") String filePath, @Param("noteId") String noteId);

    void deleteByNoteIdIn(List<String> noteIds);
}
