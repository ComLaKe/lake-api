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
import com.ulake.api.models.Document;
import com.ulake.api.payload.request.AttachFileRequest;
import com.ulake.api.payload.response.MessageResponse;
import com.ulake.api.repository.DocumentRepository;
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
public class DocumentController {
	@Autowired
	private DocumentRepository documentRepository;
	
    @Autowired
    private FileRepository fileRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LocalPermissionService permissionService;

//    TODO BUG 
	@Operation(summary = "Get all Document", description = "This can only be done by logged in file.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "document" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostFilter("hasPermission(filterObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
	@GetMapping("/documents")
//	public ResponseEntity<List<Document>> getAllDocuments(){
//		try {
//			List<Document> documents = new ArrayList<Document>();			
//			documentRepository.findAll().forEach(documents::add);
//			if (documents.isEmpty()) {
//				return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
//			}
//			
//			return new ResponseEntity<>(documents, HttpStatus.OK);
//		} catch (Exception e) {
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
	public List<Document> getAllDocuments(){
		return documentRepository.findAll();
	}

	@Operation(summary = "Get a document by id", description = "This can only be done by logged in file. Only File with Read permission or Admin permission can use.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "document" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Document.class))),
			@ApiResponse(responseCode = "400", description = "Invalid id supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "Document not found", content = @Content) })
//	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
//    @PostAuthorize("hasPermission(returnObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#id, 'com.ulake.api.models.Document', 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
	@GetMapping("/documents/{id}")
	public ResponseEntity<Document> getDocumentById(@PathVariable("id") long id) {
		  Optional<Document> documentData = documentRepository.findById(id);
		  if (documentData.isPresent()) {
		      return new ResponseEntity<>(documentData.get(), HttpStatus.OK);
		  } else {
		      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		  }
	}
	
	@Operation(summary = "Get a document by title", description = "This can only be done by logged in file. Only File with Read permission or Admin permission can use.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "document" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Document.class))),
			@ApiResponse(responseCode = "400", description = "Invalid title supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "Document not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @PostAuthorize("hasPermission(returnObject, 'READ') or hasPermission(returnObject, 'ADMINISTRATION')")
	@GetMapping("/documents/search/{title}")
	public Document getDocumentByTitle(@PathVariable("title") String title) {
		  return documentRepository.findByTitle(title);
	}
	
	@Operation(summary = "Add an document", description = "This can only be done by logged in user.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "document" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Status OK")
			})
	@PostMapping("/documents")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<Document> createDocument(@RequestBody Document document) {
	    try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
	    	document.setOwner(userRepository.findByEmail(userDetails.getEmail()));
	    	Document _document = documentRepository.save(document);
	        System.out.println(document);
	        permissionService.addPermissionForUser(document, BasePermission.ADMINISTRATION, authentication.getName());
	      return new ResponseEntity<>(_document, HttpStatus.CREATED);
	    } catch (Exception e) {
	      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@Operation(summary = "Update a document by ID", description = "This can only be done by logged in file and those who have permissoin Write or Admin for Document.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "document" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/documents/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#document, 'WRITE') or hasPermission(returnObject, 'ADMINISTRATION')")
	public ResponseEntity<Document> updateDocument(@PathVariable("id") long id, @RequestBody Document document) {
	  Optional<Document> documentData = documentRepository.findById(id);
	  if (documentData.isPresent()) {
	    	Document _document = documentData.get();	    	
		    _document.setTitle(document.getTitle());
		    _document.setDescription(document.getDescription());
		    _document.setLanguage(document.getLanguage());
		    _document.setSource(document.getSource());
		    _document.setTopics(document.getTopics());
	      return new ResponseEntity<>(documentRepository.save(_document), HttpStatus.OK);
	  } else {
	      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	  }
	}
	
	@Operation(summary = "Delete a document", description = "This can only be done by logged in user.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "document" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "400", description = "Invalid group ID supplied"),
			@ApiResponse(responseCode = "404", description = "Group not found")
	})
	@DeleteMapping("/documents/id/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#document, 'WRITE') or hasPermission(returnObject, 'ADMINISTRATION')")
	public ResponseEntity<Document> deleteGroupById(@PathVariable("id") long id){
		try {
			documentRepository.deleteById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} 
		catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
//	@Operation(summary = "Add a file to a document", description = "This can only be done by admin.", 
//			security = { @SecurityRequirement(name = "bearer-key") },
//			tags = { "document" })
//	@ApiResponses(value = @ApiResponse(description = "successful operation"))
//	@PutMapping("/documents/{title}")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#document, 'WRITE') or hasPermission(returnObject, 'ADMINISTRATION')")
//	public ResponseEntity<?> attachFile(@PathVariable("title") String title, @Valid @RequestBody AttachFileRequest attachFileRequest) {
//		Document documentData = documentRepository.findByTitle(title);
//		
//		List<String> strFiles = attachFileRequest.getFile();
//		List<File> files = new ArrayList<File>();
//		
//		if (strFiles == null) {
//		    return ResponseEntity.badRequest().body(new MessageResponse("Error: Please enter at least a file!"));
//		} else {
//			strFiles.forEach(file -> {
//				File documentFile = fileRepository.findByName(file)
//						.orElseThrow(() -> new RuntimeException("Error: File is not found."));
//				files.add(documentFile);
//			});
//		}
//		documentData.setFiles(files);
//		documentRepository.save(documentData);
//	  return new ResponseEntity<>(documentRepository.save(documentData), HttpStatus.OK);
//	}
}
