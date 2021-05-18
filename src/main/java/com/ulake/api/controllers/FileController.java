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

import com.ulake.api.models.Folder;
import com.ulake.api.models.File;
import com.ulake.api.models.File;
import com.ulake.api.models.User;
import com.ulake.api.payload.request.AddMemberRequest;
import com.ulake.api.payload.response.MessageResponse;
import com.ulake.api.repository.FolderRepository;
import com.ulake.api.repository.FileRepository;
import com.ulake.api.repository.UserRepository;
import com.ulake.api.security.services.FilesStorageService;
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
	private FolderRepository folderRepository;
	
    @Autowired
    private UserRepository userRepository;

    @Autowired
	private FileRepository fileRepository;
	
    @Autowired
    FilesStorageService storageService;
    
	@Operation(summary = "Add a file", description = "This can only be done by logged in user having the folder permissions.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "file" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Status OK")
			})
	@PostMapping("/folder/{folderTitle}/files")
//	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
//    @PostAuthorize("hasPermission(returnObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<File> createFile(
			@PathVariable("folderTitle") String folderTitle, 
			@RequestBody File file) {
	    try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
	    	file.setUser(userRepository.findByEmail(userDetails.getEmail()));
	    	file.setFolder(folderRepository.findByTitle(folderTitle));
	    	File _file = fileRepository.save(file);
	      return new ResponseEntity<>(_file, HttpStatus.CREATED);
	    } catch (Exception e) {
	      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@Operation(summary = "Update a file name by ID", description = "This can only be done by admin.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "file" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/files/id/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<File> updateFile(@PathVariable("id") long id, @RequestBody File file) {
	  Optional<File> fileData = fileRepository.findById(id);
	  if (fileData.isPresent()) {
	    	File _file = fileData.get();	    	
		    _file.setName(file.getName());
		    _file.setCid(file.getCid());
		    _file.setMimeType(file.getMimeType());
		    _file.setSize(file.getSize());
		    _file.setUpdateDate(file.getUpdateDate());
	      return new ResponseEntity<>(fileRepository.save(_file), HttpStatus.OK);
	  } else {
	      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	  }
	}
	
//	@Operation(summary = "Attach a file to a folder", description = "This can only be done by admin.", 
//			security = { @SecurityRequirement(name = "bearer-key") },
//			tags = { "file" })
//	@ApiResponses(value = @ApiResponse(description = "successful operation"))
//	@PutMapping("/folders/{folderId}/files/id/{fileId}")
//	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
//	public ResponseEntity<File> attachFile(
//			@PathVariable("folderId") long folderId, 
//			@PathVariable("fileId") long fileId) {
//	  Optional<File> fileData = fileRepository.findById(fileId);
//	  if (fileData.isPresent()) {
//	    	File _file = fileData.get();	    	
//	      return new ResponseEntity<>(fileRepository.save(_file), HttpStatus.OK);
//	  } else {
//	      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//	  }
//	}
	
	@Operation(summary = "Get a file by ID", description = "This can only be done by logged in user.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "file" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content) })
	@GetMapping("/files/id/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<File> getFileById(@PathVariable("id") long id) {
	  Optional<File> fileData = fileRepository.findById(id);
	  if (fileData.isPresent()) {
	      return new ResponseEntity<>(fileData.get(), HttpStatus.OK);
	  } else {
	      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	  }
	}
	
	@Operation(summary = "Get a file by name", description = "This can only be done by logged in user.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "file" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid name supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content) })
	@GetMapping("/files/{name}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<File> getFileByName(@PathVariable("name") String name){
		Optional<File> file = fileRepository.findByName(name);
		if (file != null) {
			return new ResponseEntity<>(file.get(),HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@Operation(summary = "Get all files", description = "This can only be done by logged in user.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "file" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@GetMapping("/files/all")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<List<File>> getAllFiles(@RequestParam(required=false) String name){
		try {
			List<File> files = new ArrayList<File>();
			
			if (name == null)
				fileRepository.findAll().forEach(files::add);
			else
				fileRepository.findByNameContaining(name).forEach(files::add);
			
			if (files.isEmpty()) {
				return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
			}
			
			return new ResponseEntity<>(files, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Operation(summary = "Delete a file", description = "This can only be done by admin.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "file" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "400", description = "Invalid file ID supplied"),
			@ApiResponse(responseCode = "404", description = "File not found")
	})
	@DeleteMapping("/files/id/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<File> deleteFileById(@PathVariable("id") long id){
		try {
			fileRepository.deleteById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} 
		catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
