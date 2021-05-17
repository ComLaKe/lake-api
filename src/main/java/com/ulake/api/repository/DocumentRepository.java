package com.ulake.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import com.ulake.api.models.Document;


public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findAll();
    
	Document findByTitle(String title);
	
	Document findByTitleContaining(String title);
    
    Document findById(Integer id);
}
