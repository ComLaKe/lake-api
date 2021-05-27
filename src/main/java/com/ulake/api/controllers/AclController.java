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

import com.ulake.api.models.File;
import com.ulake.api.models.Group;
import com.ulake.api.models.User;
import com.ulake.api.payload.response.MessageResponse;
import com.ulake.api.repository.FileRepository;
import com.ulake.api.repository.GroupRepository;
import com.ulake.api.repository.UserRepository;
import com.ulake.api.security.services.LocalPermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class AclController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
	private FileRepository fileRepository;
    
    @Autowired
	private GroupRepository groupRepository;
    
    @Autowired
    private LocalPermissionService permissionService;

	
	@Operation(summary = "Grant Permission For User", description = "This can only by done by Admin or File Owner.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "acl" })
	@ApiResponses(value = {
	@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
	@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
	@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	@PostMapping("/acl/grant_permssion/user")
	public ResponseEntity<?> grantPermissionForUser(
			@RequestParam Long fileId,
			@RequestParam Long userId,
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
			case "READ":
				permissionService.addPermissionForUser(file, BasePermission.READ, user.getUsername());
		}
	    return ResponseEntity.ok(new MessageResponse("Grant Permssion for User successful!"));
	}
	
	@Operation(summary = "Grant Permission For Group", description = "This can only by done by Admin or File Owner.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "acl" })
	@ApiResponses(value = {
	@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
	@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
	@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	@PostMapping("/acl/grant_permssion/group")
	public ResponseEntity<?> grantPermissionForGroup(
			@RequestParam Long fileId,
			@RequestParam Long groupId,
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
			case "READ":
				permissionService.addPermissionForAuthority(file, BasePermission.READ, group.getName());
		}
	    return ResponseEntity.ok(new MessageResponse("Grant Permssion for Group successful!"));
	}
	
	@Operation(summary = "Remove ALL Permission For User", description = "This can only by done by Admin or File Owner.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "acl" })
	@ApiResponses(value = {
	@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
	@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
	@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	@PostMapping("/acl/remove/user")
	public ResponseEntity<?> removePermissionForUser(
			@RequestParam Long fileId,
			@RequestParam Long userId) {
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
	
	@Operation(summary = "Remove ALL Permission For Group", description = "This can only by done by Admin or File Owner.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "acl" })
	@ApiResponses(value = {
	@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = File.class))),
	@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
	@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasPermission(#file, 'WRITE')")
	@PostMapping("/acl/remove/group")
	public ResponseEntity<?> removePermissionForGroup(
			@RequestParam Long fileId,
			@RequestParam Long groupId) {
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
}
