package com.ulake.api.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.ulake.api.models.ERole;
import com.ulake.api.models.Group;
import com.ulake.api.models.Role;
import com.ulake.api.models.User;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class GroupController {
	@Autowired
	GroupRepository groupRepository;
	
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
	
	@Operation(summary = "Update a group by ID", description = "This can only be done by admin.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "group" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/groups/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Group> updateGroup(@PathVariable("id") long id, @RequestBody Group group) {
	  Optional<Group> groupData = groupRepository.findById(id);
	  if (groupData.isPresent()) {
	    	Group _group = groupData.get();	    	
//		    Set<User> strUsers = _group.getUsers();
		    _group.setName(group.getName());
	      return new ResponseEntity<>(groupRepository.save(_group), HttpStatus.OK);
	  } else {
	      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	  }
	}
	
	@Operation(summary = "Get all groups", description = "This can only be done by admin.", 
			security = { @SecurityRequirement(name = "bearer-key") },
			tags = { "group" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@GetMapping("/groups/all")
	@PreAuthorize("hasRole('ADMIN')")
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
}
