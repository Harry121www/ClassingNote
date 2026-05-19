package com.classnote.repository;

import com.classnote.entity.Draft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DraftRepository extends JpaRepository<Draft, String> {

    Draft findTopByOrderBySavedAtDesc();

    void deleteAll();
}
