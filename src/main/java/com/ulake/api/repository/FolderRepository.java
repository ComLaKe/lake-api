package com.ulake.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ulake.api.models.Folder;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long>{
	Optional<Folder> findByName(String name);
		
	Page<Folder> findByNameContaining(String name, Pageable pageable);
	
	Page<Folder> findByName(String name, Pageable pageable);

}
