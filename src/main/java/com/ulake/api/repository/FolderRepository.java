package com.ulake.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import com.ulake.api.models.Folder;


public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findAll();
    
	Folder findByTitle(String title);
	
	Folder findByTitleContaining(String title);
    
    Folder findById(Integer id);
}
