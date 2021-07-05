package com.ulake.api.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.ulake.api.constant.AclSourceType;
import com.ulake.api.constant.AclTargetType;
import com.ulake.api.constant.PermType;
import com.ulake.api.models.Acl;
import com.ulake.api.models.File;
import com.ulake.api.models.Folder;
import com.ulake.api.models.User;
import com.ulake.api.payload.request.UpdateFolderRequest;
import com.ulake.api.repository.AclRepository;
import com.ulake.api.repository.FileRepository;
import com.ulake.api.repository.FolderRepository;
import com.ulake.api.repository.UserRepository;
import com.ulake.api.security.services.ComlakeCoreService;
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

	@Autowired
	ComlakeCoreService coreService;

	// TODO: Bulk upload files
	@Operation(summary = "Upload a file", description = "This can only be done by logged in user having the file permissions.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "File" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Status OK") })
	@PostMapping(value = "/files", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@PostAuthorize("hasPermission(returnObject, 'READ')")
	public File uploadFile(@RequestParam(required = true, value = "file") MultipartFile file,
			@RequestParam(required = true, value = "topics", defaultValue = "unlisted") List<String> topics,
			@RequestParam(required = false, value = "language", defaultValue = "English") String language,
			@RequestParam(required = true, value = "source", defaultValue = "unspecified") String source)
			throws IOException {
		// Find out who is the current logged in user
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		User fileOwner = userRepository.findByEmail(userDetails.getEmail());

		String fileName = StringUtils.getFilename(file.getOriginalFilename());
		String fileMimeType = file.getContentType();
		Long fileSize = file.getSize();
		byte[] fileData = file.getBytes();

		File fileInfo = new File(fileOwner, fileName);

		String cid = coreService.postFile(fileData, fileSize, fileMimeType);
		fileInfo.setCid(cid);

		String datasetId = coreService.addDataset(cid, fileName, source, topics, fileSize, fileMimeType, language);
		fileInfo.setDatasetId(datasetId);

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
	public File updateFile(@PathVariable("id") Long id, @RequestBody UpdateFolderRequest updateFileRequest)
			throws JsonMappingException, JsonProcessingException {
		File _file = fileRepository.findById(id).get();
		String currDatasetId = _file.getDatasetId();
		String name = updateFileRequest.getName();
		String source = updateFileRequest.getSource();
		List<String> topics = updateFileRequest.getTopics();
		String language = updateFileRequest.getLanguage();

		String newDatasetId = coreService.updateDataset(currDatasetId, name, source, topics, language);
		_file.setDatasetId(newDatasetId);
		_file.setName(updateFileRequest.getName());

		return fileRepository.save(_file);
	}

	@Operation(summary = "Add a file to a folder", description = "This can only be done by user who has write permission to file.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Folder" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/folders/{folderId}/files/{fileId}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	public ResponseEntity<Folder> addFileToFolder(@PathVariable("folderId") Long folderId,
			@PathVariable("fileId") Long fileId) throws JsonMappingException, JsonProcessingException {
		Optional<Folder> folderData = folderRepository.findById(folderId);
		Optional<File> fileData = fileRepository.findById(fileId);
		if (folderData.isPresent() && fileData.isPresent()) {
			Folder _folder = folderData.get();
			File _file = fileData.get();
			_file.setFolder(_folder);
			String cid = coreService.cpToDir(_file.getCid(), _folder.getCid(), _file.getName());
			_folder.setCid(cid);
			_file.setIsFirstNode(false);
			fileRepository.save(_file);
			return new ResponseEntity<>(folderRepository.save(_folder), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Get a file by ID", description = "This can only be done by logged in user.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "File" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content) })
	@GetMapping("/files/{id}")
	@PreAuthorize("(hasAnyRole('ADMIN','USER')) or (hasPermission(#id, 'com.ulake.api.models.File', 'READ'))")
	public ResponseEntity<?> getFileById(@PathVariable("id") Long id) {
		Optional<File> fileData = fileRepository.findById(id);
		if (fileData.isPresent()) {
			File _file = fileData.get();
			return new ResponseEntity<>(coreService.findByDatasetId(_file.getDatasetId()), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Get content by topics", description = "This can only be done by logged in user.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Content" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content) })
	@GetMapping("/find/topics")
	@PreAuthorize("(hasAnyRole('ADMIN','USER')) or (hasPermission(#id, 'com.ulake.api.models.File', 'READ')) or (hasPermission(#id, 'com.ulake.api.models.Folder', 'READ'))")
	public ResponseEntity<?> getContentByTopic(@RequestParam("topics") List<String> topics) {
		return new ResponseEntity<>(coreService.findByTopics(topics), HttpStatus.OK);
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
			aclRepository.removeBySourceIdAndSourceType(id, AclSourceType.FILE);
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

	@Operation(summary = "Get all first node content", description = "This can only be done by logged in user with file permissions.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Content" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@GetMapping("/content")
	@PreAuthorize("(hasAnyRole('ADMIN','USER')) or (hasPermission(#file, 'READ')) or (hasPermission(#folder, 'READ'))")
	public List<Object> getFirstNodeContent() {
		List<Object> content = new ArrayList<>();
		folderRepository.findByIsFirstNodeTrue().forEach(content::add);
		fileRepository.findByIsFirstNodeTrue().forEach(content::add);
		return content;
	}

	@Operation(summary = "List directory by ID", description = "This can only be done by logged in user with file permissions.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Content" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@GetMapping("/folders/ls/{folderId}")
	@PreAuthorize("(hasAnyRole('ADMIN','USER')) or (hasPermission(#folder, 'READ'))")
	public List<Object> getContentById(@PathVariable Long folderId) {
		List<Object> content = new ArrayList<>();
		Folder _folder = folderRepository.findById(folderId).get();
		_folder.getSubfolders().forEach(content::add);
		_folder.getFiles().forEach(content::add);
		return content;
	}

	@Operation(summary = "Find all contents by name containing", description = "This can only be done by logged in user with file permissions.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Content" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@GetMapping("/find/name/{name}")
	@PreAuthorize("(hasAnyRole('ADMIN','USER')) or (hasPermission(#file, 'READ')) or (hasPermission(#folder, 'READ'))")
	public List<Object> findByName(@PathVariable String name) {
		List<Object> content = new ArrayList<>();
		fileRepository.findByNameContaining(name).forEach(content::add);
		folderRepository.findByNameContaining(name).forEach(content::add);
		return content;
	}

	@Operation(summary = "Get File Data", description = "This can only be done by logged in user and those who have read permssions of file.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "File" })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'READ')")
	@GetMapping("/files/data/{id}")
	public ResponseEntity<?> getFileData(@PathVariable Long id) {
		File fileInfo = fileRepository.findById(id).get();
		String cid = fileInfo.getCid();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getName() + "\"")
				.body(coreService.getFileData(cid));
	}

	@Operation(summary = "Get All Files by Folder Id", description = "This can only be done by logged in user and those who have read permssions of file.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "File" })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'READ')")
	@GetMapping("/folder/{folderId}/files")
	public List<File> getAllFilesByFolderId(@PathVariable(value = "folderId") Long folderId) {
		return fileRepository.findByFolderId(folderId);
	}
}
