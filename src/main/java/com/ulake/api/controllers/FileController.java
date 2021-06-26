package com.ulake.api.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.http.ResponseEntity.HeadersBuilder;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulake.api.constant.AclSourceType;
import com.ulake.api.constant.AclTargetType;
import com.ulake.api.constant.PermType;
import com.ulake.api.models.Acl;
import com.ulake.api.models.CLFile;
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

import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;

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

	@Value("${app.coreBasePath}")
	private String coreBasePath;

	private RestTemplate restTemplate = new RestTemplate();

	@Operation(summary = "Upload a file", description = "This can only be done by logged in user having the file permissions.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "File" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Status OK") })
	@PostMapping(value = "/files", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@PostAuthorize("hasPermission(returnObject, 'READ')")
	public CLFile uploadFile(@RequestParam("file") MultipartFile file,
			@RequestHeader(required = true, value = "topics") List<String> topics,
			@RequestHeader(required = false, value = "language") String language,
			@RequestHeader(required = true, value = "source") String source) throws IOException {
		// Find out who is the current logged in user
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		User fileOwner = userRepository.findByEmail(userDetails.getEmail());
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		String fileMimeType = file.getContentType();
		Long fileSize = file.getSize();
		byte[] fileData = file.getBytes();

		CLFile fileInfo = new CLFile(fileOwner, fileName, fileMimeType, fileSize, fileData);

		// Append metadata to file
		String topicsStr = String.join(",", topics);
		fileInfo.setTopics(topicsStr);

		fileInfo.setLanguage(language);
		fileInfo.setSource(source);

		// Request to core POST /file - Add the file to the underlying file system
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.set("Content-Length", fileSize.toString());
		headers.set("Content-Type", fileMimeType);

		HttpEntity<byte[]> entity = new HttpEntity<>(fileData, headers);

		ResponseEntity<byte[]> response = restTemplate.postForEntity(coreBasePath + "/file", entity, byte[].class);

		// Get and save the response cid
		ObjectMapper mapperCreate = new ObjectMapper();
		JsonNode rootCreate = mapperCreate.readTree(response.getBody());
		String cid = rootCreate.path("cid").asText();
		fileInfo.setCid(cid);

		// Request to core POST /add - Add Metadata for the directory
		HttpHeaders headersJson = new HttpHeaders();
		headersJson.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		JSONObject dataset = new JSONObject();
		dataset.put("file", cid);
		dataset.put("description", fileName);
		dataset.put("source", source);
		dataset.put("topics", new JSONArray(topics));
		if (language != null) {
			dataset.put("language", language);
		}

		HttpEntity<String> requestDataset = new HttpEntity<String>(dataset.toString(), headers);
		ResponseEntity<String> responseDataset = restTemplate.postForEntity(coreBasePath + "/add", requestDataset,
				String.class);

		// Get and save the response datasetId
		ObjectMapper mapperDataset = new ObjectMapper();
		JsonNode rootDataset = mapperDataset.readTree(responseDataset.getBody());
		String datasetId = rootDataset.path("id").asText();
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
	public CLFile updateFile(@PathVariable("id") Long id, @RequestBody CLFile file) {
		CLFile _file = fileRepository.findById(id).get();
		_file.setName(file.getName());
		_file.setCid(file.getCid());
		_file.setSource(file.getSource());
		_file.setLanguage(file.getLanguage());
		_file.setTopics(file.getTopics());
		return fileRepository.save(_file);
	}

	@Operation(summary = "Add a file to a folder", description = "This can only be done by user who has write permission to file.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Folder" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/folders/{folderId}/files/{fileId}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	public CLFile addFileToFolder(@PathVariable("folderId") Long folderId, @PathVariable("fileId") Long fileId) {
//		  TODO Will fail if not found		
		Folder folder = folderRepository.findById(folderId).get();
		CLFile _file = fileRepository.findById(fileId).get();
		_file.setFolder(folder);
		return fileRepository.save(_file);
	}

	@Operation(summary = "Get a file by ID", description = "This can only be done by logged in user.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "File" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = CLFile.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content) })
	@GetMapping("/files/{id}")
	@PreAuthorize("(hasAnyRole('ADMIN','USER')) or (hasPermission(#id, 'com.ulake.api.models.File', 'READ'))")
	public CLFile getFileById(@PathVariable("id") Long id) {
		return fileRepository.findById(id).get();
	}

	@Operation(summary = "Delete a file by ID", description = "This can only be done by logged in user.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "File" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = CLFile.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "File not found", content = @Content) })
	@DeleteMapping("/files/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	public ResponseEntity<CLFile> deleteFileById(@PathVariable("id") long id) {
		try {
			CLFile file = fileRepository.findById(id).get();
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
	public List<CLFile> getAllFiles(@RequestParam(required = false) String name) {
		List<CLFile> files = new ArrayList<CLFile>();
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
	public void getFileData(@PathVariable Long id) throws IOException {
		CLFile fileInfo = fileRepository.findById(id).get();
		String FILE_URL = coreBasePath + "/file" + fileInfo.getCid();

		// Optional Accept header
		RequestCallback requestCallback = request -> request.getHeaders()
				.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));

		// Streams the response instead of loading it all in memory
//	    ResponseExtractor<Void> responseExtractor = response -> {
//	        // Here you can write the inputstream to a file or any other place
//	        Path path = Paths.get(fileInfo.getName());
//	        Files.copy(response.getBody(), path);
//	        return null;
//	    };

//		return ResponseEntity.ok()
//				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getName() + "\"")
//				.body(fileInfo.getData());

		restTemplate.execute(FILE_URL, HttpMethod.GET, requestCallback, clientHttpResponse -> {
			Files.copy(clientHttpResponse.getBody(), Paths.get(fileInfo.getName()));
			return null;
		});
	}

	@Operation(summary = "Get File Data", description = "This can only be done by logged in user and those who have read permssions of file.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "File" })
	@GetMapping("/files/data")
	public void testDownLoadBigFile() throws IOException {
		// File address to be downloaded
		String FILE_URL = "http://localhost:8090/file/QmbboxGZtfKZNSqnSXyqRzqn7hwbNh8JjHzAysH8PLa5vF";
		// Local path to save the file
		String targetPath = "C:\\Users\\thaonp\\Downloads\\testfiles\\download\\interject.txt";

		File file = restTemplate.execute(FILE_URL, HttpMethod.GET, null, clientHttpResponse -> {
			File ret = File.createTempFile("download", "tmp");
			StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(ret));
			return ret;
		});

//		return ResponseEntity.ok()
//		.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getName() + "\"")
//		.body(fileInfo.getData());

	}

	@Operation(summary = "Get All Files by Folder Id", description = "This can only be done by logged in user and those who have read permssions of file.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "File" })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'READ')")
	@GetMapping("/folder/{folderId}/files")
	public List<CLFile> getAllFilesByFolderId(@PathVariable(value = "folderId") Long folderId) {
		return fileRepository.findByFolderId(folderId);
	}
}
