package com.ulake.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import com.ulake.api.models.Document;


public interface DocumentRepository extends JpaRepository<Document, Long> {
    @PostFilter("hasPermission(filterObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
    List<Document> findAll();
    
    @PostAuthorize("hasPermission(returnObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
	Document findByTitle(String title);
	
    @PostAuthorize("hasPermission(returnObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
	Document findByTitleContaining(String title);
    
    @PostAuthorize("hasPermission(returnObject, 'READ')")
    Document findById(Integer id);
}
