package com.ulake.api.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulake.api.constant.AclSourceType;
import com.ulake.api.constant.AclTargetType;
import com.ulake.api.constant.PermType;
import com.ulake.api.models.Acl;
import com.ulake.api.models.Folder;
import com.ulake.api.models.User;
import com.ulake.api.payload.request.CreateFolderRequest;
import com.ulake.api.payload.request.UpdateFolderRequest;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class FolderController {
	@Autowired
	FolderRepository folderRepository;

	@Autowired
	FileRepository fileRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	AclRepository aclRepository;

	@Autowired
	private LocalPermissionService permissionService;

	@Autowired
	ComlakeCoreService coreService;

	@Operation(summary = "Add a folder", description = "This can only be done by logged in user.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Folder" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Status OK") })
	@PostMapping("/folders")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@PostAuthorize("hasPermission(returnObject, 'READ')")
	public Folder createFolder(@RequestBody CreateFolderRequest createFolderRequest) throws IOException {
		// Get current principal
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		User folderCreator = userRepository.findByEmail(userDetails.getEmail());

		// Create Folder
		Folder _folder = new Folder(folderCreator, createFolderRequest.getName());

		String cid = coreService.postFolder();
		_folder.setCid(cid);

		// Request to core POST /add - Add Metadata for the directory
		String datasetId = coreService.addDataset(cid, createFolderRequest.getName(), createFolderRequest.getSource(),
				createFolderRequest.getTopics(), null, null, createFolderRequest.getLanguage());
		_folder.setDatasetId(datasetId);

		// Save to Repository
		folderRepository.save(_folder);

		// Add ACL permissions
		permissionService.addPermissionForAuthority(_folder, BasePermission.READ, "ROLE_ADMIN");
		permissionService.addPermissionForAuthority(_folder, BasePermission.WRITE, "ROLE_ADMIN");
		permissionService.addPermissionForUser(_folder, BasePermission.READ, authentication.getName());
		permissionService.addPermissionForUser(_folder, BasePermission.WRITE, authentication.getName());

		// Create Acl
		aclRepository.save(new Acl(_folder.getId(), folderCreator.getUsername(), AclSourceType.FOLDER, AclTargetType.USER,
				PermType.READ));
		aclRepository.save(new Acl(_folder.getId(), folderCreator.getUsername(), AclSourceType.FOLDER, AclTargetType.USER,
				PermType.WRITE));

		return _folder;
	}

	@Operation(summary = "Update a folder by ID", description = "Only name and parentId could be updated .This can only be done by users who has write permission for folders.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Folder" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/folders/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE')")
	public ResponseEntity<Folder> updateFolder(@PathVariable("id") long id,
			@RequestBody UpdateFolderRequest updateFolderRequest) throws JsonMappingException, JsonProcessingException {
		Optional<Folder> folderData = folderRepository.findById(id);
		if (folderData.isPresent()) {
			Folder _folder = folderData.get();
			String currDatasetId = _folder.getDatasetId();
			String name = updateFolderRequest.getName();
			String source = updateFolderRequest.getSource();
			List<String> topics = updateFolderRequest.getTopics();
			String language = updateFolderRequest.getLanguage();

			String newDatasetId = coreService.updateDataset(currDatasetId, name, source, topics, language);
			_folder.setDatasetId(newDatasetId);
			_folder.setName(updateFolderRequest.getName());
			
			return new ResponseEntity<>(folderRepository.save(_folder), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Add a subfolder to folder", description = "This can only be done by users who has write permission for folders.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Folder" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/folders/{folderId}/subfolders/{subfolderId}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE')")
	public ResponseEntity<Folder> addSubfolder(@PathVariable("folderId") Long folderId,
			@PathVariable("subfolderId") Long subfolderId) throws JsonMappingException, JsonProcessingException {
		Optional<Folder> subfolderData = folderRepository.findById(subfolderId);
		Optional<Folder> folderData = folderRepository.findById(folderId);
		if (subfolderData.isPresent() && folderData.isPresent()) {
			Folder _folder = folderData.get();
			Folder _subfolder = subfolderData.get();
			_folder.addSubfolder(_subfolder);
			_subfolder.setParent(_folder);
			String cid = coreService.cpToDir(_subfolder.getCid(), _folder.getCid(), _subfolder.getName());
			_folder.setCid(cid);
			_subfolder.setIsFirstNode(false);
			return new ResponseEntity<>(folderRepository.save(_folder), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Get a folder by ID", description = "This can only be done by users who has read permission for folders.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Folder" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Folder.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "Folder not found", content = @Content) })
	@GetMapping("/folders/{id}")
	@PreAuthorize("(hasAnyRole('ADMIN','USER')) or (hasPermission(#id, 'com.ulake.api.models.Folder', 'READ'))")
	public ResponseEntity<?> getFolderById(@PathVariable("id") long id) throws JsonProcessingException {
		Optional<Folder> folderData = folderRepository.findById(id);
		if (folderData.isPresent()) {
			Folder _folder = folderData.get();	
			Object[] dataset = coreService.findByDatasetId(_folder.getDatasetId());
			ObjectMapper mapper = new ObjectMapper();
			String jsonString = mapper.writeValueAsString(_folder);
			JSONObject jo = new JSONObject(jsonString);; 
			String datasetJsonString = mapper.writeValueAsString(dataset[0]);
			JSONObject datasetJson = new JSONObject(datasetJsonString);; 
			jo.put("language", datasetJson.getString("language"));
			jo.put("source", datasetJson.getString("source"));
			jo.put("topics", datasetJson.getJSONArray("topics"));			
			return ResponseEntity.status(HttpStatus.OK).body(jo.toString());
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Get a list of content inside folder by ID", description = "This can only be done by users who has read permission for folders.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Folder" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Folder.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "Folder not found", content = @Content) })
	@GetMapping("/folders/content/{id}")
	@PreAuthorize("(hasAnyRole('ADMIN','USER')) or (hasPermission(#id, 'com.ulake.api.models.Folder', 'READ'))")
	public ResponseEntity<?> getContentFolderById(@PathVariable("id") long id)
			throws JsonMappingException, JsonProcessingException {
		Optional<Folder> folderData = folderRepository.findById(id);
		if (folderData.isPresent()) {
			Folder _folder = folderData.get();
			JsonNode root = coreService.listContent(_folder.getCid());
			return new ResponseEntity<>(root, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Get all folders", description = "This can only be done by users who has read permission for folders.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Folder" })
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
			@SecurityRequirement(name = "bearer-key") }, tags = { "Folder" })
	@ApiResponses(value = { @ApiResponse(responseCode = "400", description = "Invalid folder ID supplied"),
			@ApiResponse(responseCode = "404", description = "Folder not found") })
	@DeleteMapping("/folders/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE')")
	public ResponseEntity<Folder> deleteFolderById(@PathVariable("id") long id) {
		try {
			Folder folder = folderRepository.findById(id).get();
			folderRepository.deleteById(id);
			permissionService.removeAcl(folder);
			aclRepository.removeBySourceIdAndSourceType(id, AclSourceType.FOLDER);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
