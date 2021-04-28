package com.ulake.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ulake.api.models.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
	Boolean existsByName(String name);
	
	List<Group> findByName(String name);
	List<Group> findByNameContaining(String name);
}