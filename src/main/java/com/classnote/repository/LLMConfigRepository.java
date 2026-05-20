package com.classnote.repository;

import com.classnote.entity.LLMConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LLMConfigRepository extends JpaRepository<LLMConfigEntity, String> {
}
