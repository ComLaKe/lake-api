package com.ulake.api.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import org.springframework.web.bind.annotation.RestController;

import com.ulake.api.models.File;
import com.ulake.api.models.Group;
import com.ulake.api.models.Folder;
import com.ulake.api.payload.request.AttachFileRequest;
import com.ulake.api.payload.response.MessageResponse;
import com.ulake.api.repository.FolderRepository;
import com.ulake.api.repository.FileRepository;
import com.ulake.api.repository.UserRepository;
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
public class FolderController {
	@Autowired
	private FolderRepository folderRepository;
	
    @Autowired
    private FileRepository fileRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LocalPermissionService permissionService;

//    TODO BUG 
	@Operation(summary = "Get all Folder", description = "This can only be done by logged in file.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "folder" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostFilter("hasPermission(filterObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
	@GetMapping("/folders")
//	public ResponseEntity<List<Folder>> getAllFolders(){
//		try {
//			List<Folder> folders = new ArrayList<Folder>();			
//			folderRepository.findAll().forEach(folders::add);
//			if (folders.isEmpty()) {
//				return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
//			}
//			
//			return new ResponseEntity<>(folders, HttpStatus.OK);
//		} catch (Exception e) {
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
	public List<Folder> getAllFolders(){
		return folderRepository.findAll();
	}

	@Operation(summary = "Get a folder by id", description = "This can only be done by logged in file. Only File with Read permission or Admin permission can use.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "folder" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Folder.class))),
			@ApiResponse(responseCode = "400", description = "Invalid id supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "Folder not found", content = @Content) })
//	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
//    @PostAuthorize("hasPermission(returnObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#id, 'com.ulake.api.models.Folder', 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
	@GetMapping("/folders/{id}")
	public ResponseEntity<Folder> getFolderById(@PathVariable("id") long id) {
		  Optional<Folder> folderData = folderRepository.findById(id);
		  if (folderData.isPresent()) {
		      return new ResponseEntity<>(folderData.get(), HttpStatus.OK);
		  } else {
		      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		  }
	}
	
	@Operation(summary = "Get a folder by title", description = "This can only be done by logged in file. Only File with Read permission or Admin permission can use.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "folder" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Folder.class))),
			@ApiResponse(responseCode = "400", description = "Invalid title supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "Folder not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostAuthorize("hasPermission(returnObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
	@GetMapping("/folders/search/{title}")
	public Folder getFolderByTitle(@PathVariable("title") String title) {
		  return folderRepository.findByTitle(title);
	}
	
	@Operation(summary = "Add an folder", description = "This can only be done by logged in user.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "folder" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Status OK")
			})
	@PostMapping("/folders")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<Folder> createFolder(@RequestBody Folder folder) {
	    try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
	    	folder.setOwner(userRepository.findByEmail(userDetails.getEmail()));
	    	Folder _folder = folderRepository.save(folder);
	        System.out.println(folder);
	        permissionService.addPermissionForUser(folder, BasePermission.ADMINISTRATION, authentication.getName());
	      return new ResponseEntity<>(_folder, HttpStatus.CREATED);
	    } catch (Exception e) {
	      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@Operation(summary = "Update a folder by ID", description = "This can only be done by logged in file and those who have permissoin Write or Admin for Folder.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "folder" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/folders/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE') or hasPermission(returnObject, 'ADMINISTRATION')")
	public ResponseEntity<Folder> updateFolder(@PathVariable("id") long id, @RequestBody Folder folder) {
	  Optional<Folder> folderData = folderRepository.findById(id);
	  if (folderData.isPresent()) {
	    	Folder _folder = folderData.get();	    	
		    _folder.setTitle(folder.getTitle());
		    _folder.setDescription(folder.getDescription());
		    _folder.setLanguage(folder.getLanguage());
		    _folder.setSource(folder.getSource());
		    _folder.setTopics(folder.getTopics());
	      return new ResponseEntity<>(folderRepository.save(_folder), HttpStatus.OK);
	  } else {
	      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	  }
	}
	
	@Operation(summary = "Delete a folder", description = "This can only be done by logged in user.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "folder" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "400", description = "Invalid group ID supplied"),
			@ApiResponse(responseCode = "404", description = "Group not found")
	})
	@DeleteMapping("/folders/id/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE') or hasPermission(returnObject, 'ADMINISTRATION')")
	public ResponseEntity<Folder> deleteGroupById(@PathVariable("id") long id){
		try {
			folderRepository.deleteById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} 
		catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
//	@Operation(summary = "Add a file to a folder", description = "This can only be done by admin.", 
//			security = { @SecurityRequirement(name = "bearer-key") },
//			tags = { "folder" })
//	@ApiResponses(value = @ApiResponse(description = "successful operation"))
//	@PutMapping("/folders/{title}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE') or hasPermission(returnObject, 'ADMINISTRATION')")
//	public ResponseEntity<?> attachFile(@PathVariable("title") String title, @Valid @RequestBody AttachFileRequest attachFileRequest) {
//		Folder folderData = folderRepository.findByTitle(title);
//		
//		List<String> strFiles = attachFileRequest.getFile();
//		List<File> files = new ArrayList<File>();
//		
//		if (strFiles == null) {
//		    return ResponseEntity.badRequest().body(new MessageResponse("Error: Please enter at least a file!"));
//		} else {
//			strFiles.forEach(file -> {
//				File folderFile = fileRepository.findByName(file)
//						.orElseThrow(() -> new RuntimeException("Error: File is not found."));
//				files.add(folderFile);
//			});
//		}
//		folderData.setFiles(files);
//		folderRepository.save(folderData);
//	  return new ResponseEntity<>(folderRepository.save(folderData), HttpStatus.OK);
//	}
}
