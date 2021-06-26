package com.ulake.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import com.ulake.api.models.CLFile;

public interface FileRepository extends JpaRepository<CLFile, Long> {
	Boolean existsByName(String name);

	@PostFilter("hasPermission(filterObject, 'READ')")
	List<CLFile> findByNameContaining(String name);

	@PostFilter("hasPermission(filterObject, 'READ')")
	Page<CLFile> findByNameContaining(String name, Pageable pageable);

	@PostFilter("hasPermission(filterObject, 'READ')")
	List<CLFile> findAll();

	@PreAuthorize("hasPermission(#id, 'com.ulake.api.models.File', 'READ')")
	CLFile findByName(String name);

	@PreAuthorize("hasPermission(#id, 'com.ulake.api.models.File', 'READ')")
	Optional<CLFile> findById(Long id);

	@PreAuthorize("hasPermission(#id, 'com.ulake.api.models.File', 'WRITE')")
	CLFile removeById(Long id);

	@PostFilter("hasPermission(filterObject, 'READ')")
	List<CLFile> findByFolderId(Long folderId);

//    @PreAuthorize("hasPermission(#id, 'com.ulake.api.models.File', 'READ')")
//    Optional<File> findByIdAndFolderId(Long id, Long folderId);

//    @SuppressWarnings("unchecked")
//    @PreAuthorize("hasPermission(#file, 'WRITE') or hasPermission(returnObject, 'ADMINISTRATION')")
//    File save(@Param("file")File file);
}
