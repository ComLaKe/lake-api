package com.ulake.api.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

	@Operation(summary = "Get All Permissions", description = "This can only by done by Admin or File Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@GetMapping("/acl/perm")
	public List<Acl> getAllPermission(@RequestParam(required = false) Long user,
			@RequestParam(required = false) Long group) {
		List<Acl> acls = new ArrayList<Acl>();
		if ((user == null) & (group == null))
			aclRepository.findAll().forEach(acls::add);
		else if (user != null) {
			aclRepository.findByTargetTypeAndTargetId(AclTargetType.USER, user).forEach(acls::add);
		} else if (group != null) {
			aclRepository.findByTargetTypeAndTargetId(AclTargetType.GROUP, group).forEach(acls::add);
		} else {
			aclRepository.findByTargetTypeAndTargetId(AclTargetType.GROUP, group).forEach(acls::add);
			aclRepository.findByTargetTypeAndTargetId(AclTargetType.USER, user).forEach(acls::add);
		}
		return acls;
	}

	@Operation(summary = "Grant File Permission For User", description = "This can only by done by Admin or File Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	@PostMapping("/acl/grant/file/user")
	public ResponseEntity<?> grantFilePermissionForUser(@RequestParam Long fileId, @RequestParam Long userId,
			@RequestParam String perm) {
		File file = fileRepository.findById(fileId).get();
		User user = userRepository.findById(userId).get();
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
	@PostMapping("/acl/grant/file/group")
	public ResponseEntity<?> grantFilePermissionForGroup(@RequestParam Long fileId, @RequestParam Long groupId,
			@RequestParam String perm) {
		File file = fileRepository.findById(fileId).get();
		Group group = groupRepository.findById(groupId).get();
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
	@DeleteMapping("/acl/files/remove/user")
	public ResponseEntity<?> removeAllFilePermissionForUser(@RequestParam Long fileId, @RequestParam Long userId) {
		File file = fileRepository.findById(fileId).get();
		User user = userRepository.findById(userId).get();
		permissionService.removeAllPermissionForUser(file, user.getUsername());
		aclRepository.removeBySourceIdAndTargetIdAndSourceTypeAndTargetType(fileId, userId, AclSourceType.FILE,
				AclTargetType.USER);
		return ResponseEntity.ok(new MessageResponse("Remove all Permssions for User successful!"));
	}

	@Operation(summary = "Remove ALL File Permission For Group", description = "This can only by done by Admin or File Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	@DeleteMapping("/acl/files/remove/group")
	public ResponseEntity<?> removeAllFilePermissionForGroup(@RequestParam Long fileId, @RequestParam Long groupId) {
		File file = fileRepository.findById(fileId).get();
		Group group = groupRepository.findById(groupId).get();
		permissionService.removeAllPermissionForAuthority(file, group.getName());
		aclRepository.removeBySourceIdAndTargetIdAndSourceTypeAndTargetType(fileId, groupId, AclSourceType.FILE,
				AclTargetType.GROUP);
		return ResponseEntity.ok(new MessageResponse("Remove All Permissions for Group successful!"));
	}

	@Operation(summary = "Grant Folder Permission For User", description = "This can only by done by Admin or Folder Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Folder.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE')")
	@PostMapping("/acl/grant/folder/user")
	public ResponseEntity<?> grantFolderPermissionForUser(@RequestParam Long folderId, @RequestParam Long userId,
			@RequestParam String perm) {
		Folder folder = folderRepository.findById(folderId).get();
		User user = userRepository.findById(userId).get();
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
	@PostMapping("/acl/grant/folder/group")
	public ResponseEntity<?> grantFolderPermissionForGroup(@RequestParam Long folderId, @RequestParam Long groupId,
			@RequestParam String perm) {
		Folder folder = folderRepository.findById(folderId).get();
		Group group = groupRepository.findById(groupId).get();
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
	@DeleteMapping("/acl/folders/remove/user")
	public ResponseEntity<?> removeAllFolderPermissionForUser(@RequestParam Long folderId, @RequestParam Long userId) {
		Folder folder = folderRepository.findById(folderId).get();
		User user = userRepository.findById(userId).get();
		permissionService.removeAllPermissionForUser(folder, user.getUsername());
		aclRepository.removeBySourceIdAndTargetIdAndSourceTypeAndTargetType(folderId, userId, AclSourceType.FOLDER,
				AclTargetType.USER);
		return ResponseEntity.ok(new MessageResponse("Remove all Permssions for User successful!"));
	}

	@Operation(summary = "Remove ALL Folder Permission For Group", description = "This can only by done by Admin or Folder Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Folder.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE')")
	@DeleteMapping("/acl/folders/remove/group")
	public ResponseEntity<?> removeAllFolderPermissionForGroup(@RequestParam Long folderId,
			@RequestParam Long groupId) {
		Folder folder = folderRepository.findById(folderId).get();
		Group group = groupRepository.findById(groupId).get();
		permissionService.removeAllPermissionForAuthority(folder, group.getName());
		aclRepository.removeBySourceIdAndTargetIdAndSourceTypeAndTargetType(folderId, groupId, AclSourceType.FOLDER,
				AclTargetType.GROUP);
		return ResponseEntity.ok(new MessageResponse("Remove All Permissions for Group successful!"));
	}
}
