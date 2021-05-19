package com.ulake.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ulake.api.models.File;

public interface FileRepository extends JpaRepository<File, Long> {
	Boolean existsByName(String name);
	
	Optional<File> findByName(String name);
	
	List<File> findByNameContaining(String name);
	
	List<File> findByTitle(String title);
}	

