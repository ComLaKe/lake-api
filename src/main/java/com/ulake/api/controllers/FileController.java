package com.ulake.api.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
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
import org.springframework.web.multipart.MultipartFile;

import com.ulake.api.constant.AclSourceType;
import com.ulake.api.constant.AclTargetType;
import com.ulake.api.constant.PermType;
import com.ulake.api.models.Acl;
import com.ulake.api.models.File;
import com.ulake.api.models.Folder;
import com.ulake.api.models.User;
import com.ulake.api.repository.AclRepository;
import com.ulake.api.repository.FileRepository;
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

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class FileController {
	@Autowired
	private FileRepository fileRepository;

	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AclRepository aclRepository;

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

	@Operation(summary = "Upload a file", description = "This can only be done by logged in user having the file permissions.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "File" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Status OK") })
	@PostMapping(value = "/files", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@PostAuthorize("hasPermission(returnObject, 'READ')")
	public File uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
		// Find out who is the current logged in user
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		User fileOwner = userRepository.findByEmail(userDetails.getEmail());
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		String fileMimeType = file.getContentType();
		Long fileSize = file.getSize();
		byte[] fileData = file.getBytes();
		File fileInfo = new File(fileOwner, fileName, fileMimeType, fileSize, fileData);

		// Save File Metadata in our db;
		fileRepository.save(fileInfo);

		// Add ACL WRITE and READ Permission For Admin and File Owner
		permissionService.addPermissionForAuthority(fileInfo, BasePermission.READ, "ROLE_ADMIN");
		permissionService.addPermissionForAuthority(fileInfo, BasePermission.WRITE, "ROLE_ADMIN");
		permissionService.addPermissionForUser(fileInfo, BasePermission.READ, authentication.getName());
		permissionService.addPermissionForUser(fileInfo, BasePermission.WRITE, authentication.getName());

		aclRepository.save(
				new Acl(fileInfo.getId(), fileOwner.getId(), AclSourceType.FILE, AclTargetType.USER, PermType.READ));
		aclRepository.save(
				new Acl(fileInfo.getId(), fileOwner.getId(), AclSourceType.FILE, AclTargetType.USER, PermType.WRITE));

		return fileInfo;
	}
	
	@Operation(summary = "Update a file by ID", description = "This can only be done by user who has write permission to file.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "File" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/files/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	public File updateFile(@PathVariable("id") Long id, @RequestBody File file) {
//	  TODO Will fail if not found
		File _file = fileRepository.findById(id).get();
		_file.setName(file.getName());
		_file.setCid(file.getCid());
		_file.setSource(file.getSource());
		_file.setTopics(file.getTopics());
		return fileRepository.save(_file);
	}

	@Operation(summary = "Add a file to a folder", description = "This can only be done by user who has write permission to file.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Folder" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/folders/{folderId}/files/{fileId}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	public File addFileToFolder(@PathVariable("folderId") Long folderId, @PathVariable("fileId") Long fileId) {
//		  TODO Will fail if not found		
		Folder folder = folderRepository.findById(folderId).get();
		File _file = fileRepository.findById(fileId).get();
		_file.setFolder(folder);
		return fileRepository.save(_file);
	}

	@Operation(summary = "Get a file by ID", description = "This can only be done by logged in user.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "File" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content) })
	@GetMapping("/files/{id}")
	@PreAuthorize("(hasAnyRole('ADMIN','USER')) or (hasPermission(#id, 'com.ulake.api.models.File', 'READ'))")
	public File getFileById(@PathVariable("id") Long id) {
		return fileRepository.findById(id).get();
	}

	@Operation(summary = "Delete a file by ID", description = "This can only be done by logged in user.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "File" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content) })
	@DeleteMapping("/files/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	public ResponseEntity<File> deleteFileById(@PathVariable("id") long id) {
		try {
			File file = fileRepository.findById(id).get();
			fileRepository.deleteById(id);
//			aclRepository.deleteAllBySourceIdAndSourceType(id, "File");
			permissionService.removeAcl(file);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Get all files", description = "This can only be done by logged in user with file permissions.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "File" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@GetMapping("/files")
	@PreAuthorize("(hasAnyRole('ADMIN','USER')) or (hasPermission(#file, 'READ'))")
	public List<File> getAllFiles(@RequestParam(required = false) String name) {
		List<File> files = new ArrayList<File>();
		if (name == null)
			fileRepository.findAll().forEach(files::add);
		else
			fileRepository.findByNameContaining(name).forEach(files::add);
		return files;
	}

	@Operation(summary = "Get File Data", description = "This can only be done by logged in user and those who have read permssions of file.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "File" })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'READ')")
	@GetMapping("/files/data/{id}")
	public ResponseEntity<byte[]> getFileData(@PathVariable Long id) {
		File fileInfo = fileRepository.findById(id).get();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getName() + "\"")
				.body(fileInfo.getData());
	}

	@Operation(summary = "Get All Files by Folder Id", description = "This can only be done by logged in user and those who have read permssions of file.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "File" })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'READ')")
	@GetMapping("/folder/{folderId}/files")
	public List<File> getAllFilesByFolderId(@PathVariable(value = "folderId") Long folderId) {
		return fileRepository.findByFolderId(folderId);
	}
}
