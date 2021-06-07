package com.ulake.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import com.ulake.api.models.Folder;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long>{
	Boolean existsByName(String name);
	
	@PostFilter("hasPermission(filterObject, 'READ')")
	List<Folder> findByNameContaining(String name);
		
	@PostFilter("hasPermission(filterObject, 'READ')")
	Page<Folder> findByNameContaining(String name, Pageable pageable);
	
	@PostFilter("hasPermission(filterObject, 'READ')")
	List<Folder> findAll();
	
    @PreAuthorize("hasPermission(#id, 'com.ulake.api.models.Folder', 'READ')")
    Folder findByName(String name);

    @PreAuthorize("hasPermission(#id, 'com.ulake.api.models.Folder', 'READ')")
    Optional<Folder> findById(Long id);
    
    @PreAuthorize("hasPermission(#id, 'com.ulake.api.models.Folder', 'WRITE')")
    Folder removeById(Long id);        
}
