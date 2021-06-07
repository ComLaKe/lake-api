package com.ulake.api.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ulake.api.repository.FolderRepository;
import com.ulake.api.repository.UserRepository;
import com.ulake.api.security.services.LocalPermissionService;
import com.ulake.api.security.services.impl.UserDetailsImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.ulake.api.models.Folder;
import com.ulake.api.models.User;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class FolderController {
	@Autowired
	FolderRepository folderRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	private LocalPermissionService permissionService;

//	private Sort.Direction getSortDirection(String direction) {
//	    if (direction.equals("ASC")) {
//	      return Sort.Direction.ASC;
//	    } else if (direction.equals("DESC")) {
//	      return Sort.Direction.DESC;
//	    }
//
//	    return Sort.Direction.ASC;
//	}

	@Operation(summary = "Add a folder", description = "This can only be done by logged in user.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "folder" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Status OK") })
	@PostMapping("/folders")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@PostAuthorize("hasPermission(returnObject, 'READ')")
	public Folder createFolder(@RequestBody Folder folder) {
		// Get current principal
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		User folderCreator = userRepository.findByEmail(userDetails.getEmail());
		// Create Folder
		Folder _folder = new Folder(folderCreator, folder.getName());
		// Save to Repository
		folderRepository.save(_folder);
		// Add ACL permissions
		permissionService.addPermissionForAuthority(_folder, BasePermission.READ, "ROLE_ADMIN");
		permissionService.addPermissionForAuthority(_folder, BasePermission.WRITE, "ROLE_ADMIN");
		permissionService.addPermissionForUser(_folder, BasePermission.READ, authentication.getName());
		permissionService.addPermissionForUser(_folder, BasePermission.WRITE, authentication.getName());
		return _folder;
	}

	@Operation(summary = "Update a folder by ID", description = "Only name and parentId could be updated .This can only be done by users who has write permission for folders.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "folder" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/folders/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE')")
	public ResponseEntity<Folder> updateFolder(@PathVariable("id") long id, @RequestBody Folder folder) {
		Optional<Folder> folderData = folderRepository.findById(id);
		if (folderData.isPresent()) {
			Folder _folder = folderData.get();
			_folder.setName(folder.getName());
			_folder.setParentId(folder.getParentId());
			return new ResponseEntity<>(folderRepository.save(_folder), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Add a subfolder name to folder", description = "This can only be done by users who has write permission for folders.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "folder" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/folders/{folderId}/subfolders/{subfolderId}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE')")
	public ResponseEntity<Folder> addSubfolder(@PathVariable("folderId") Long folderId,
			@PathVariable("subfolderId") Long subfolderId) {
		Optional<Folder> subfolderData = folderRepository.findById(subfolderId);
		Optional<Folder> folderData = folderRepository.findById(folderId);
		if (subfolderData.isPresent() && folderData.isPresent()) {
			Folder _subfolder = subfolderData.get();
			_subfolder.setParentId(folderData.get().getId());
			return new ResponseEntity<>(folderRepository.save(_subfolder), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Get a folder by ID", description = "This can only be done by users who has read permission for folders.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "folder" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Folder.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "Folder not found", content = @Content) })
	@GetMapping("/folders/{id}")
	@PreAuthorize("(hasAnyRole('ADMIN','USER')) or (hasPermission(#id, 'com.ulake.api.models.Folder', 'READ'))")
	public ResponseEntity<Folder> getFolderById(@PathVariable("id") long id) {
		Optional<Folder> folderData = folderRepository.findById(id);
		if (folderData.isPresent()) {
			return new ResponseEntity<>(folderData.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Get all folders", description = "This can only be done by users who has read permission for folders.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "folder" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@GetMapping("/folders")
	@PreAuthorize("(hasAnyRole('ADMIN','USER')) or (hasPermission(#folder, 'READ'))")
	public List<Folder> getAllFolders(@RequestParam(required = false) String name) {
		List<Folder> folders = new ArrayList<Folder>();
		if (name == null)
			folderRepository.findAll().forEach(folders::add);
		else
			folderRepository.findByNameContaining(name).forEach(folders::add);
		return folders;
	}

	@Operation(summary = "Delete a folder", description = "This can only be done by users who has write permission for folders.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "folder" })
	@ApiResponses(value = { @ApiResponse(responseCode = "400", description = "Invalid folder ID supplied"),
			@ApiResponse(responseCode = "404", description = "Folder not found") })
	@DeleteMapping("/folders/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE')")
	public ResponseEntity<Folder> deleteFolderById(@PathVariable("id") long id) {
		try {
			Folder folder = folderRepository.findById(id).get();
			folderRepository.deleteById(id);
			permissionService.removeAcl(folder);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
