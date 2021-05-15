package com.ulake.api.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import com.ulake.api.repository.GroupRepository;
import com.ulake.api.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.ulake.api.models.ERole;
import com.ulake.api.models.Group;
import com.ulake.api.models.Role;
import com.ulake.api.models.User;
import com.ulake.api.payload.request.AddMemberRequest;
import com.ulake.api.payload.response.MessageResponse;
import com.ulake.api.payload.response.MessageResponse;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class GroupController {
	@Autowired
	GroupRepository groupRepository;

	@Autowired
	UserRepository userRepository;

	@Operation(summary = "Add an user group", description = "This can only be done by admin.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "group" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Status OK")
			})
	@PostMapping("/groups")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Group> createGroup(@RequestBody Group group) {
	    try {
	    	Group _group = groupRepository
	          .save(new Group(group.getName()));
	      return new ResponseEntity<>(_group, HttpStatus.CREATED);
	    } catch (Exception e) {
	      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@Operation(summary = "Update a group name by ID", description = "This can only be done by admin.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "group" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/groups/id/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Group> updateGroup(@PathVariable("id") long id, @RequestBody Group group) {
	  Optional<Group> groupData = groupRepository.findById(id);
	  if (groupData.isPresent()) {
	    	Group _group = groupData.get();	    	
		    _group.setName(group.getName());
	      return new ResponseEntity<>(groupRepository.save(_group), HttpStatus.OK);
	  } else {
	      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	  }
	}
	
	@Operation(summary = "Get a group by ID", description = "This can only be done by logged in user.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "group" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@GetMapping("/groups/id/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<Group> getGroupById(@PathVariable("id") long id) {
	  Optional<Group> groupData = groupRepository.findById(id);
	  if (groupData.isPresent()) {
	      return new ResponseEntity<>(groupData.get(), HttpStatus.OK);
	  } else {
	      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	  }
	}
	
	@Operation(summary = "Get a group by name", description = "This can only be done by logged in user.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "group" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = User.class))),
			@ApiResponse(responseCode = "400", description = "Invalid username supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
	@GetMapping("/groups/{name}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<Group> getGroupByName(@PathVariable("name") String name){
		Optional<Group> group = groupRepository.findByName(name);
		if (group != null) {
			return new ResponseEntity<>(group.get(),HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@Operation(summary = "Add a user to a group", description = "This can only be done by admin.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "group" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/groups/{name}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> addMember(@PathVariable("name") String name, @Valid @RequestBody AddMemberRequest addMemberRequest) {
	  Optional<Group> groupData = groupRepository.findByName(name);
	  if (groupData.isPresent()) {
	    	Group _group = groupData.get();
	    	
			Set<String> strUsers = addMemberRequest.getUser();
			Set<User> users = new HashSet<>();
			
			if (strUsers == null) {
			    return ResponseEntity.badRequest().body(new MessageResponse("Error: Please enter at least a user!"));
			} else {
				strUsers.forEach(user -> {
					User groupUser = userRepository.findByUsername(user)
							.orElseThrow(() -> new RuntimeException("Error: User is not found."));
					users.add(groupUser);
				});
			}
			
			_group.setUsers(users);
			groupRepository.save(_group);
	      return new ResponseEntity<>(groupRepository.save(_group), HttpStatus.OK);
	  } else {
	      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	  }
//		return ResponseEntity.ok(new MessageResponse("Add a user to a group successfully!"));
	}
	
	@Operation(summary = "Get all groups", description = "This can only be done by logged in user.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "group" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@GetMapping("/groups/all")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<List<Group>> getAllGroups(@RequestParam(required=false) String name){
		try {
			List<Group> groups = new ArrayList<Group>();
			
			if (name == null)
				groupRepository.findAll().forEach(groups::add);
			else
				groupRepository.findByNameContaining(name).forEach(groups::add);
			
			if (groups.isEmpty()) {
				return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
			}
			
			return new ResponseEntity<>(groups, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Operation(summary = "Delete a group", description = "This can only be done by admin.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "group" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "400", description = "Invalid group ID supplied"),
			@ApiResponse(responseCode = "404", description = "Group not found")
	})
	@DeleteMapping("/groups/id/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Group> deleteGroupById(@PathVariable("id") long id){
		try {
			groupRepository.deleteById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} 
		catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
