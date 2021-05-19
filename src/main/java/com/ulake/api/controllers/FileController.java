package com.ulake.api.controllers;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
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
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;

import com.ulake.api.models.File;
import com.ulake.api.models.User;
import com.ulake.api.payload.request.AddMemberRequest;
import com.ulake.api.payload.request.CreateFileRequest;
import com.ulake.api.payload.response.MessageResponse;
import com.ulake.api.repository.FileRepository;
import com.ulake.api.repository.UserRepository;
import com.ulake.api.security.services.FilesStorageService;
import com.ulake.api.security.services.LocalPermissionService;
import com.ulake.api.security.services.UserDetailsImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class FileController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
	private FileRepository fileRepository;
	
    @Autowired
    FilesStorageService storageService;
    
    @Autowired
    private LocalPermissionService permissionService;

	@Operation(summary = "Add a file", description = "This can only be done by logged in user having the file permissions.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "file" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Status OK")
			})
	@PostMapping("/files")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostAuthorize("hasPermission(returnObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
//	public File createFile(
//			@RequestBody CreateFileRequest createFileRequest) {
//		File file = new File(createFileRequest.getCid(), createFileRequest.getName(),
//				createFileRequest.getMimeType(), createFileRequest.getSize());
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
//		file.setUser(userRepository.findByEmail(userDetails.getEmail()));
//        permissionService.addPermissionForUser(file, BasePermission.ADMINISTRATION, authentication.getName());
//    	return fileRepository.save(file);
//	}
	public File createFile(@RequestBody File file) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    	file.setOwner(userRepository.findByEmail(userDetails.getEmail()));
    	File _file = fileRepository.save(file);
        System.out.println(file);
        permissionService.addPermissionForUser(file, BasePermission.ADMINISTRATION, authentication.getName());
        return _file;
	}
	
	@Operation(summary = "Update a file name by ID", description = "This can only be done by admin.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "file" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/files/id/{id}")
//	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PreAuthorize("(hasRole('ADMIN') or hasRole('USER')) AND (hasPermission(#file, 'WRITE') or hasPermission(returnObject, 'ADMINISTRATION'))")
	public File updateFile(@PathVariable("id") Long id, @RequestBody File file) {
	  Optional<File> fileData = fileRepository.findById(id);
//	  TODO Will fail if not found
	  File _file = fileData.get();	    	
	  _file.setName(file.getName());
	  _file.setCid(file.getCid());
	  _file.setMimeType(file.getMimeType());
	  _file.setSize(file.getSize());
	  _file.setUpdateDate(file.getUpdateDate());
      return fileRepository.save(_file);
	}
	
	@Operation(summary = "Get a file by ID", description = "This can only be done by logged in user.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "file" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content) })
	@GetMapping("/files/id/{id}")
//	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PreAuthorize("(hasRole('ADMIN') or hasRole('USER')) AND (hasPermission(#id, 'com.ulake.api.models.File', 'READ') or hasPermission(returnObject, 'ADMINISTRATION'))")
	public File getFileById(@PathVariable("id") Long id) {
	  Optional<File> fileData = fileRepository.findById(id);
	  File _file = fileData.get();
	  return _file;
	}
	
	@Operation(summary = "Get all files", description = "This can only be done by logged in user.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "file" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@GetMapping("/files/all")
	@PreAuthorize("(hasRole('ADMIN') or hasRole('USER')) AND (hasPermission(filterObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION'))")
//    @PostFilter("hasPermission(filterObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
	public List<File> getAllFiles(@RequestParam(required=false) String name){
		List<File> files = new ArrayList<File>();
		if (name == null)
			fileRepository.findAll().forEach(files::add);
		else
			fileRepository.findByNameContaining(name).forEach(files::add);
		return files;
	}
	
//	@Operation(summary = "Delete a file", description = "This can only be done by admin.", 
//			security = { @SecurityRequirement(name = "bearer-key") },
//			tags = { "file" })
//	@ApiResponses(value = {
//			@ApiResponse(responseCode = "400", description = "Invalid file ID supplied"),
//			@ApiResponse(responseCode = "404", description = "File not found")
//	})
//	@DeleteMapping("/files/id/{id}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE') or hasPermission(returnObject, 'ADMINISTRATION')")
////	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
//	public File deleteFileById(@PathVariable("id") long id){
//		File fileData = fileRepository.deleteById(id);
//		File _file = fileData.get();
//		return _file;
//	}
}
