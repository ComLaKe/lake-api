package com.ulake.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import com.ulake.api.models.File;

public interface FileRepository extends JpaRepository<File, Long> {
	Boolean existsByName(String name);
		
	@PostFilter("hasPermission(filterObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
	List<File> findByNameContaining(String name);
	
	@PostFilter("hasPermission(filterObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
	List<File> findByName(String title);
	
	@PostFilter("hasPermission(filterObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
	List<File> findAll();
	
    @PreAuthorize("hasPermission(#id, 'com.ulake.api.models.File', 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
    Optional<File> findById(Long id);
    
//    @SuppressWarnings("unchecked")
//    @PreAuthorize("hasPermission(#file, 'WRITE') or hasPermission(returnObject, 'ADMINISTRATION')")
//    File save(@Param("file")File file);
}	


