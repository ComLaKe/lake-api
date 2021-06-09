package com.ulake.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import com.ulake.api.models.File;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
	Boolean existsByName(String name);

	@PostFilter("hasPermission(filterObject, 'READ')")
	List<File> findByNameContaining(String name);

	@PostFilter("hasPermission(filterObject, 'READ')")
	Page<File> findByNameContaining(String name, Pageable pageable);

	@PostFilter("hasPermission(filterObject, 'READ')")
	List<File> findAll();

	@PreAuthorize("hasPermission(#id, 'com.ulake.api.models.File', 'READ')")
	File findByName(String name);

	@PreAuthorize("hasPermission(#id, 'com.ulake.api.models.File', 'READ')")
	Optional<File> findById(Long id);

	@PreAuthorize("hasPermission(#id, 'com.ulake.api.models.File', 'WRITE')")
	File removeById(Long id);

	@PostFilter("hasPermission(filterObject, 'READ')")
	List<File> findByFolderId(Long folderId);

//    @PreAuthorize("hasPermission(#id, 'com.ulake.api.models.File', 'READ')")
//    Optional<File> findByIdAndFolderId(Long id, Long folderId);

//    @SuppressWarnings("unchecked")
//    @PreAuthorize("hasPermission(#file, 'WRITE') or hasPermission(returnObject, 'ADMINISTRATION')")
//    File save(@Param("file")File file);
}
