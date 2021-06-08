package com.ulake.api.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ulake.api.constant.AclSourceType;
import com.ulake.api.constant.AclTargetType;
import com.ulake.api.constant.PermType;
import com.ulake.api.models.Acl;
import com.ulake.api.models.File;
import com.ulake.api.models.Folder;
import com.ulake.api.models.Group;
import com.ulake.api.models.User;
import com.ulake.api.payload.response.MessageResponse;
import com.ulake.api.repository.AclRepository;
import com.ulake.api.repository.FileRepository;
import com.ulake.api.repository.FolderRepository;
import com.ulake.api.repository.GroupRepository;
import com.ulake.api.repository.UserRepository;
import com.ulake.api.security.services.LocalPermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class AclController {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FileRepository fileRepository;

	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private AclRepository aclRepository;

	@Autowired
	private LocalPermissionService permissionService;

	@Operation(summary = "Grant File Permission For User", description = "This can only by done by Admin or File Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	@PostMapping("/acl/files/grant_permssion/user")
	public ResponseEntity<?> grantFilePermissionForUser(@RequestParam Long fileId, @RequestParam Long userId,
			@RequestParam String perm) {
		Optional<File> fileData = fileRepository.findById(fileId);
		File file = fileData.get();
		if (!fileData.isPresent()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: File Not Found!"));
		}
		Optional<User> userData = userRepository.findById(userId);
		if (!userData.isPresent()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: User Not Found!"));
		}
		User user = userData.get();
		switch (perm) {
		case "WRITE":
			permissionService.addPermissionForUser(file, BasePermission.WRITE, user.getUsername());
			aclRepository
					.save(new Acl(file.getId(), user.getId(), AclSourceType.FILE, AclTargetType.USER, PermType.WRITE));
		case "READ":
			permissionService.addPermissionForUser(file, BasePermission.READ, user.getUsername());
			aclRepository
					.save(new Acl(file.getId(), user.getId(), AclSourceType.FILE, AclTargetType.USER, PermType.READ));
		}
		return ResponseEntity.ok(new MessageResponse("Grant Permssion for User successful!"));
	}

	@Operation(summary = "Grant File Permission For Group", description = "This can only by done by Admin or File Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	@PostMapping("/acl/files/grant_permssion/group")
	public ResponseEntity<?> grantFilePermissionForGroup(@RequestParam Long fileId, @RequestParam Long groupId,
			@RequestParam String perm) {
		Optional<File> fileData = fileRepository.findById(fileId);
		File file = fileData.get();
		if (!fileData.isPresent()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: File Not Found!"));
		}
		Optional<Group> groupData = groupRepository.findById(groupId);
		if (!groupData.isPresent()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: User Not Found!"));
		}
		Group group = groupData.get();
		switch (perm) {
		case "WRITE":
			permissionService.addPermissionForAuthority(file, BasePermission.WRITE, group.getName());
			aclRepository.save(
					new Acl(file.getId(), group.getId(), AclSourceType.FILE, AclTargetType.GROUP, PermType.WRITE));
		case "READ":
			permissionService.addPermissionForAuthority(file, BasePermission.READ, group.getName());
			aclRepository
					.save(new Acl(file.getId(), group.getId(), AclSourceType.FILE, AclTargetType.GROUP, PermType.READ));
		}
		return ResponseEntity.ok(new MessageResponse("Grant Permssion for Group successful!"));
	}

	@Operation(summary = "Remove ALL File Permission For User", description = "This can only by done by Admin or File Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	@PostMapping("/acl/files/remove/user")
	public ResponseEntity<?> removeAllFilePermissionForUser(@RequestParam Long fileId, @RequestParam Long userId) {
		Optional<File> fileData = fileRepository.findById(fileId);
		File file = fileData.get();
		if (!fileData.isPresent()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: File Not Found!"));
		}
		Optional<User> userData = userRepository.findById(userId);
		if (!userData.isPresent()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: User Not Found!"));
		}
		User user = userData.get();
		permissionService.removeAllPermissionForUser(file, user.getUsername());
		return ResponseEntity.ok(new MessageResponse("Remove all Permssions for User successful!"));
	}

	@Operation(summary = "Remove ALL File Permission For Group", description = "This can only by done by Admin or File Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	@PostMapping("/acl/files/remove/group")
	public ResponseEntity<?> removeAllFilePermissionForGroup(@RequestParam Long fileId, @RequestParam Long groupId) {
		Optional<File> fileData = fileRepository.findById(fileId);
		File file = fileData.get();
		if (!fileData.isPresent()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: File Not Found!"));
		}
		Optional<Group> groupData = groupRepository.findById(groupId);
		if (!groupData.isPresent()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: User Not Found!"));
		}
		Group group = groupData.get();
		permissionService.removeAllPermissionForAuthority(file, group.getName());
		return ResponseEntity.ok(new MessageResponse("Remove All Permissions for Group successful!"));
	}

	@Operation(summary = "Grant Folder Permission For User", description = "This can only by done by Admin or Folder Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Folder.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE')")
	@PostMapping("/acl/folders/grant_permssion/user")
	public ResponseEntity<?> grantFolderPermissionForUser(@RequestParam Long folderId, @RequestParam Long userId,
			@RequestParam String perm) {
		Optional<Folder> folderData = folderRepository.findById(folderId);
		Folder folder = folderData.get();
		if (!folderData.isPresent()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Folder Not Found!"));
		}
		Optional<User> userData = userRepository.findById(userId);
		if (!userData.isPresent()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: User Not Found!"));
		}
		User user = userData.get();
		switch (perm) {
		case "WRITE":
			permissionService.addPermissionForUser(folder, BasePermission.WRITE, user.getUsername());
			aclRepository.save(
					new Acl(folder.getId(), user.getId(), AclSourceType.FOLDER, AclTargetType.USER, PermType.WRITE));
		case "READ":
			permissionService.addPermissionForUser(folder, BasePermission.READ, user.getUsername());
			aclRepository.save(
					new Acl(folder.getId(), user.getId(), AclSourceType.FOLDER, AclTargetType.USER, PermType.READ));
		}
		return ResponseEntity.ok(new MessageResponse("Grant Permssion for User successful!"));
	}

	@Operation(summary = "Grant Folder Permission For Group", description = "This can only by done by Admin or Folder Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Folder.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE')")
	@PostMapping("/acl/folders/grant_permssion/group")
	public ResponseEntity<?> grantFolderPermissionForGroup(@RequestParam Long folderId, @RequestParam Long groupId,
			@RequestParam String perm) {
		Optional<Folder> folderData = folderRepository.findById(folderId);
		Folder folder = folderData.get();
		if (!folderData.isPresent()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Folder Not Found!"));
		}
		Optional<Group> groupData = groupRepository.findById(groupId);
		if (!groupData.isPresent()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: User Not Found!"));
		}
		Group group = groupData.get();
		switch (perm) {
		case "WRITE":
			permissionService.addPermissionForAuthority(folder, BasePermission.WRITE, group.getName());
			aclRepository.save(
					new Acl(folder.getId(), group.getId(), AclSourceType.FOLDER, AclTargetType.GROUP, PermType.WRITE));
		case "READ":
			permissionService.addPermissionForAuthority(folder, BasePermission.READ, group.getName());
			aclRepository.save(
					new Acl(folder.getId(), group.getId(), AclSourceType.FOLDER, AclTargetType.GROUP, PermType.READ));
		}
		return ResponseEntity.ok(new MessageResponse("Grant Permssion for Group successful!"));
	}

	@Operation(summary = "Remove ALL Folder Permission For User", description = "This can only by done by Admin or Folder Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Folder.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE')")
	@PostMapping("/acl/folders/remove/user")
	public ResponseEntity<?> removeAllFolderPermissionForUser(@RequestParam Long folderId, @RequestParam Long userId) {
		Optional<Folder> folderData = folderRepository.findById(folderId);
		Folder folder = folderData.get();
		if (!folderData.isPresent()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Folder Not Found!"));
		}
		Optional<User> userData = userRepository.findById(userId);
		if (!userData.isPresent()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: User Not Found!"));
		}
		User user = userData.get();
		permissionService.removeAllPermissionForUser(folder, user.getUsername());
		return ResponseEntity.ok(new MessageResponse("Remove all Permssions for User successful!"));
	}

	@Operation(summary = "Remove ALL Folder Permission For Group", description = "This can only by done by Admin or Folder Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Folder.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE')")
	@PostMapping("/acl/folders/remove/group")
	public ResponseEntity<?> removeAllFolderPermissionForGroup(@RequestParam Long folderId, @RequestParam Long groupId) {
		Optional<Folder> folderData = folderRepository.findById(folderId);
		Folder folder = folderData.get();
		if (!folderData.isPresent()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Folder Not Found!"));
		}
		Optional<Group> groupData = groupRepository.findById(groupId);
		if (!groupData.isPresent()) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: User Not Found!"));
		}
		Group group = groupData.get();
		permissionService.removeAllPermissionForAuthority(folder, group.getName());
		return ResponseEntity.ok(new MessageResponse("Remove All Permissions for Group successful!"));
	}
}
