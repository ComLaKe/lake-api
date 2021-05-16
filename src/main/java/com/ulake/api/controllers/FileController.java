package com.ulake.api.controllers;


import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
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

import com.ulake.api.models.Document;
import com.ulake.api.models.File;
import com.ulake.api.models.User;
import com.ulake.api.payload.response.MessageResponse;
import com.ulake.api.security.services.UserDetailsImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class FileController {
//	private static final Logger log = LoggerFactory.getLogger(FileController.class);
//	
//	@Inject private DocumentService documentService;
//	
//	private File getFileVerifyDocumentId(Long documentId, Long fileId) {
//		File file = documentService.getFile(fileId);
//		Assert.isTrue(documentId.equals(file.getDocument().getId()), "Document ID mismatch");
//		return file;
//	}
//	
//	@Operation(summary = "Get File by ID", description = "This can only be done by admin or file owner.", 
//			security = { @SecurityRequirement(name = "bearer-key") },
//			tags = { "file" })
//	@GetMapping("/document/{documentId}/files/{fileId}")
//	public ResponseEntity<File> getFileById(@PathVariable("id") Long documentId, @PathVariable("id") Long fileId){
//		File fileData = getFileVerifyDocumentId(documentId, fileId);
//		return new ResponseEntity<>(fileData,HttpStatus.OK);
//	}
//	
//	@Operation(summary = "Create a file", description = "This can only be done by logged in user.", 
//			security = { @SecurityRequirement(name = "bearer-key") },
//			tags = { "file" })
//	@PostMapping("/documents/{documentId}/files")
//	public ResponseEntity<?> createFile(@PathVariable("documentId") Long documentId, @RequestBody File file) {
//	    try {
//			file.setDocument(documentService.getDocument(documentId, false));
//						
//			SecurityContext securityCtx = SecurityContextHolder.getContext();
//			org.springframework.security.core.Authentication authn = securityCtx.getAuthentication();
//						
//			file.setUser((User) authn.getPrincipal());
//			file.setVisible(true);
//			documentService.createFile(file);
//		  return ResponseEntity.ok(new MessageResponse("Create file successful!"));
//	    } catch (Exception e) {
//	      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//	    }
//	}
//	
////	@PutMapping("/documents/{documentId}/files/{fileId}")
////	public ResponseEntity<?> updateFile(			
////			@PathVariable("documentId") Long documentId,
////			@PathVariable("fileId") Long fileId,
////			@RequestBody File file,
////			BindingResult result) {
////		File fileData = getFileVerifyDocumentId(documentId, fileId);
////		
////		if (result.hasErrors()) {
////			log.debug("Submitted file has validation errors");
////		    return ResponseEntity.ok(new MessageResponse("Submitted file has validation errors!"));
////		}
////		
////		log.debug("File validated; updating file subject and text");
////		fileData.setName(file.getName());
////		documentService.setFileName(fileData);
////	    return ResponseEntity.ok(new MessageResponse("Update File successful!"));
////	}
//	
//	@Operation(summary = "Update a file's visible", description = "This can only be done by admin or file owner.", 
//			security = { @SecurityRequirement(name = "bearer-key") },
//			tags = { "file" })
//	@PutMapping("/document/{documentId}/files/{fileId}/visible")
//	public ResponseEntity<?> putFileVisible(			
//			@PathVariable("documentId") Long documentId,
//			@PathVariable("fileId") Long fileId,
//			@RequestParam("value") boolean value,
//			HttpServletResponse res) {
//		res.setContentType("text/plain");
//		File file = new File(documentId, fileId);
//		file.setVisible(value);
//		documentService.setFileVisible(file);
//	    return ResponseEntity.ok(new MessageResponse("Update File's visible successful!"));
//	}
//	
//	@Operation(summary = "Delete a file", description = "This can only be done by admin or file owner.", 
//		security = { @SecurityRequirement(name = "bearer-key") },
//		tags = { "file" })
//	@ApiResponses(value = {
//		@ApiResponse(responseCode = "400", description = "Invalid file ID supplied"),
//		@ApiResponse(responseCode = "404", description = "File not found")
//	})	
//	@DeleteMapping("/document/{documentId}/files/{fileId}")
//	public ResponseEntity<?> deleteMessage(			
//			@PathVariable("documentId") Long documentId,
//			@PathVariable("fileId") Long fileId) {
//		try {
//			documentService.deleteFile(getFileVerifyDocumentId(documentId, fileId));	    
//			return ResponseEntity.ok(new MessageResponse("Delete File successful!"));			
//		}
//		catch (Exception e) {	
//			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//		
//	}

}
