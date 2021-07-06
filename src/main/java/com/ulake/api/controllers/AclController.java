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
import org.springframework.web.bind.annotation.PutMapping;
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

	// I don't want to talk about any of this 	
	@Operation(summary = "Get All Permissions", description = "This can only by done by Admin or File Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@GetMapping("/acl/perm")
	public List<Acl> getAllPermission(@RequestParam(required = false) Long user,
			@RequestParam(required = false) Long group, @RequestParam(required = false) Long file,
			@RequestParam(required = false) Long folder) {
		List<Acl> acls = new ArrayList<Acl>();
		if ((user == null) & (group == null) & (file == null) & (folder == null))
			aclRepository.findAll().forEach(acls::add);
		else if (user != null) {
			aclRepository.findByTargetTypeAndTargetName(AclTargetType.USER, user).forEach(acls::add);
		} else if (group != null) {
			aclRepository.findByTargetTypeAndTargetName(AclTargetType.GROUP, group).forEach(acls::add);
		} else if (file != null) {
			aclRepository.findBySourceTypeAndSourceId(AclSourceType.FILE, file).forEach(acls::add);
		} else if (folder != null) {
			aclRepository.findBySourceTypeAndSourceId(AclSourceType.FOLDER, folder).forEach(acls::add);
		} else {
			aclRepository.findByTargetTypeAndTargetName(AclTargetType.GROUP, group).forEach(acls::add);
			aclRepository.findByTargetTypeAndTargetName(AclTargetType.USER, user).forEach(acls::add);
			aclRepository.findBySourceTypeAndSourceId(AclSourceType.FILE, file).forEach(acls::add);
			aclRepository.findBySourceTypeAndSourceId(AclSourceType.FOLDER, folder).forEach(acls::add);
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
	@PutMapping("/acl/file/user")
	public ResponseEntity<?> grantFilePermissionForUser(@RequestParam Long fileId, @RequestParam String username,
			@RequestParam String perm) {
		File file = fileRepository.findById(fileId).get();
		User user = userRepository.findByUsername(username).get();
		switch (perm) {
		case "WRITE":
			permissionService.addPermissionForUser(file, BasePermission.WRITE, user.getUsername());
			aclRepository
					.save(new Acl(file.getId(), user.getUsername(), AclSourceType.FILE, AclTargetType.USER, PermType.WRITE));
			break;
		case "READ":
			permissionService.addPermissionForUser(file, BasePermission.READ, user.getUsername());
			aclRepository
					.save(new Acl(file.getId(), user.getUsername(), AclSourceType.FILE, AclTargetType.USER, PermType.READ));
			break;
		}
		return ResponseEntity.ok(new MessageResponse("Grant Permssion for User successful!"));
	}

	@Operation(summary = "Grant Folder Permission For User", description = "This can only by done by Admin or Folder Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Folder.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE')")
	@PutMapping("/acl/folder/user")
	public ResponseEntity<?> grantFolderPermissionForUser(@RequestParam Long folderId, @RequestParam String username,
			@RequestParam String perm) {
		Folder folder = folderRepository.findById(folderId).get();
		User user = userRepository.findByUsername(username).get();
		switch (perm) {
		case "WRITE":
			permissionService.addPermissionForUser(folder, BasePermission.WRITE, user.getUsername());
			aclRepository.save(
					new Acl(folder.getId(), user.getUsername(), AclSourceType.FOLDER, AclTargetType.USER, PermType.WRITE));
			break;
		case "READ":
			permissionService.addPermissionForUser(folder, BasePermission.READ, user.getUsername());
			aclRepository.save(
					new Acl(folder.getId(), user.getUsername(), AclSourceType.FOLDER, AclTargetType.USER, PermType.READ));
			break;
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
	@PutMapping("/acl/folder/group")
	public ResponseEntity<?> grantFolderPermissionForGroup(@RequestParam Long folderId, @RequestParam String groupName,
			@RequestParam String perm) {
		Folder folder = folderRepository.findById(folderId).get();
		Group group = groupRepository.findByName(groupName).get();
		switch (perm) {
		case "WRITE":
			permissionService.addPermissionForAuthority(folder, BasePermission.WRITE, group.getName());
			aclRepository.save(
					new Acl(folder.getId(), group.getName(), AclSourceType.FOLDER, AclTargetType.GROUP, PermType.WRITE));
			break;
		case "READ":
			permissionService.addPermissionForAuthority(folder, BasePermission.READ, group.getName());
			aclRepository.save(
					new Acl(folder.getId(), group.getName(), AclSourceType.FOLDER, AclTargetType.GROUP, PermType.READ));
			break;
		}
		return ResponseEntity.ok(new MessageResponse("Grant Permssion for Group successful!"));
	}

	@Operation(summary = "Grant File Permission For Group", description = "This can only by done by Admin or File Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	@PutMapping("/acl/file/group")
	public ResponseEntity<?> grantFilePermissionForGroup(@RequestParam Long fileId, @RequestParam String groupName,
			@RequestParam String perm) {
		File file = fileRepository.findById(fileId).get();
		Group group = groupRepository.findByName(groupName).get();
		switch (perm) {
		case "WRITE":
			permissionService.addPermissionForAuthority(file, BasePermission.WRITE, group.getName());
			aclRepository.save(
					new Acl(file.getId(), group.getName(), AclSourceType.FILE, AclTargetType.GROUP, PermType.WRITE));
			break;
		case "READ":
			permissionService.addPermissionForAuthority(file, BasePermission.READ, group.getName());
			aclRepository
					.save(new Acl(file.getId(), group.getName(), AclSourceType.FILE, AclTargetType.GROUP, PermType.READ));
			break;
		}
		return ResponseEntity.ok(new MessageResponse("Grant Permssion for Group successful!"));
	}

	@Operation(summary = "Remove a File Permission For User", description = "This can only by done by Admin or File Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	@DeleteMapping("/acl/file/user")
	public ResponseEntity<?> removeFilePermissionForUser(@RequestParam("fileId") Long fileId, 
			@RequestParam("username") String username,
			@RequestParam("perm") String permStr) {
		File file = fileRepository.findById(fileId).get();
		User user = userRepository.findByUsername(username).get();
		PermType perm = PermType.valueOf(permStr);
		switch (perm) {
		case READ:
			permissionService.removePermissionForUser(file, BasePermission.READ, user.getUsername());
			aclRepository.removeBySourceIdAndTargetNameAndSourceTypeAndTargetTypeAndPerm(fileId, user.getUsername(),
					AclSourceType.FILE, AclTargetType.USER, PermType.READ);
			break;
		case WRITE:
			permissionService.removePermissionForUser(file, BasePermission.WRITE, user.getUsername());
			aclRepository.removeBySourceIdAndTargetNameAndSourceTypeAndTargetTypeAndPerm(fileId, user.getUsername(),
					AclSourceType.FILE, AclTargetType.USER, PermType.WRITE);
			break;
		}
		return ResponseEntity.ok(new MessageResponse("Remove File Permssions for User successful!"));
	}

	@Operation(summary = "Remove a File Permission For Group", description = "This can only by done by Admin or File Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	@DeleteMapping("/acl/file/group")
	public ResponseEntity<?> removeFilePermissionForGroup(@RequestParam Long fileId, @RequestParam String groupName,
			@RequestParam("perm") String permStr) {
		File file = fileRepository.findById(fileId).get();
		Group group = groupRepository.findByName(groupName).get();
		PermType perm = PermType.valueOf(permStr);
		switch (perm) {
		case READ:
			permissionService.removePermissionForAuthority(file, BasePermission.READ, group.getName());
			aclRepository.removeBySourceIdAndTargetNameAndSourceTypeAndTargetTypeAndPerm(fileId, group.getName(),
					AclSourceType.FILE, AclTargetType.GROUP, PermType.READ);
			break;
		case WRITE:
			permissionService.removePermissionForAuthority(file, BasePermission.WRITE, group.getName());
			aclRepository.removeBySourceIdAndTargetNameAndSourceTypeAndTargetTypeAndPerm(fileId, group.getName(),
					AclSourceType.FILE, AclTargetType.GROUP, PermType.WRITE);
			break;
		}
		return ResponseEntity.ok(new MessageResponse("Remove File Permssions for Group successful!"));
	}

	@Operation(summary = "Remove a Folder Permission For Group", description = "This can only by done by Admin or File Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	@DeleteMapping("/acl/folder/group")
	public ResponseEntity<?> removeFolderPermissionForGroup(@RequestParam Long folderId, @RequestParam String groupName,
			@RequestParam("perm") String permStr) {
		Folder folder = folderRepository.findById(folderId).get();
		Group group = groupRepository.findByName(groupName).get();
		PermType perm = PermType.valueOf(permStr);
		switch (perm) {
		case READ:
			permissionService.removePermissionForAuthority(folder, BasePermission.READ, group.getName());
			aclRepository.removeBySourceIdAndTargetNameAndSourceTypeAndTargetTypeAndPerm(folderId, group.getName(),
					AclSourceType.FOLDER, AclTargetType.GROUP, PermType.READ);
			break;
		case WRITE:
			permissionService.removePermissionForAuthority(folder, BasePermission.WRITE, group.getName());
			aclRepository.removeBySourceIdAndTargetNameAndSourceTypeAndTargetTypeAndPerm(folderId, group.getName(),
					AclSourceType.FOLDER, AclTargetType.GROUP, PermType.WRITE);
			break;
		}
		return ResponseEntity.ok(new MessageResponse("Remove Folder Permssions for Group successful!"));
	}

	@Operation(summary = "Remove a File Permission For User", description = "This can only by done by Admin or File Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	@DeleteMapping("/acl/folder/user")
	public ResponseEntity<?> removeFolderPermissionForUser(@RequestParam Long folderId, @RequestParam String username,
			@RequestParam("perm") String permStr) {
		Folder folder = folderRepository.findById(folderId).get();
		User user = userRepository.findByUsername(username).get();
		PermType perm = PermType.valueOf(permStr);
		switch (perm) {
		case READ:
			permissionService.removePermissionForUser(folder, BasePermission.READ, user.getUsername());
			aclRepository.removeBySourceIdAndTargetNameAndSourceTypeAndTargetTypeAndPerm(folderId, user.getUsername(),
					AclSourceType.FOLDER, AclTargetType.USER, PermType.READ);
			break;
		case WRITE:
			permissionService.removePermissionForUser(folder, BasePermission.WRITE, user.getUsername());
			aclRepository.removeBySourceIdAndTargetNameAndSourceTypeAndTargetTypeAndPerm(folderId, user.getUsername(),
					AclSourceType.FOLDER, AclTargetType.USER, PermType.WRITE);
			break;
		}
		return ResponseEntity.ok(new MessageResponse("Remove Folder Permssions for User successful!"));
	}

	@Operation(summary = "Remove ALL Folder Permission For User", description = "This can only by done by Admin or Folder Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Folder.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#folder, 'WRITE')")
	@DeleteMapping("/acl/folder/user/all")
	public ResponseEntity<?> removeAllFolderPermissionForUser(@RequestParam Long folderId, @RequestParam String username) {
		Folder folder = folderRepository.findById(folderId).get();
		User user = userRepository.findByUsername(username).get();
		permissionService.removeAllPermissionForUser(folder, user.getUsername());
		aclRepository.removeBySourceIdAndTargetNameAndSourceTypeAndTargetType(folderId, user.getUsername(), AclSourceType.FOLDER,
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
	@DeleteMapping("/acl/folder/group/all")
	public ResponseEntity<?> removeAllFolderPermissionForGroup(@RequestParam Long folderId,
			@RequestParam String groupName) {
		Folder folder = folderRepository.findById(folderId).get();
		Group group = groupRepository.findByName(groupName).get();
		permissionService.removeAllPermissionForAuthority(folder, group.getName());
		aclRepository.removeBySourceIdAndTargetNameAndSourceTypeAndTargetType(folderId, group.getName(), AclSourceType.FOLDER,
				AclTargetType.GROUP);
		return ResponseEntity.ok(new MessageResponse("Remove All Permissions for Group successful!"));
	}

	@Operation(summary = "Remove ALL File Permission For User", description = "This can only by done by Admin or File Owner.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "ACL - Access Control" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	@DeleteMapping("/acl/file/user/all")
	public ResponseEntity<?> removeAllFilePermissionForUser(@RequestParam Long fileId, @RequestParam String username) {
		File file = fileRepository.findById(fileId).get();
		User user = userRepository.findByUsername(username).get();
		permissionService.removeAllPermissionForUser(file, user.getUsername());
		aclRepository.removeBySourceIdAndTargetNameAndSourceTypeAndTargetType(fileId, user.getUsername(), AclSourceType.FILE,
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
	@DeleteMapping("/acl/file/group/all")
	public ResponseEntity<?> removeAllFilePermissionForGroup(@RequestParam Long fileId, @RequestParam String groupName) {
		File file = fileRepository.findById(fileId).get();
		Group group = groupRepository.findByName(groupName).get();
		permissionService.removeAllPermissionForAuthority(file, group.getName());
		aclRepository.removeBySourceIdAndTargetNameAndSourceTypeAndTargetType(fileId, group.getName(), AclSourceType.FILE,
				AclTargetType.GROUP);
		return ResponseEntity.ok(new MessageResponse("Remove All Permissions for Group successful!"));
	}
}
