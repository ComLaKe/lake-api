package com.ulake.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ulake.api.models.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
	Boolean existsByName(String name);
	
	Optional<Group> findByName(String name);
	
	Page<Group> findByNameContaining(String name, Pageable pageable);
	
	Page<Group> findByName(String name, Pageable pageable);
	
}