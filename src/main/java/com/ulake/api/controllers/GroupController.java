package com.ulake.api.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpHeaders;
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

import com.ulake.api.models.Group;
import com.ulake.api.models.User;
import com.ulake.api.payload.request.CreateGroupRequest;
import com.ulake.api.payload.response.MessageResponse;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class GroupController {
	@Autowired
	GroupRepository groupRepository;

	@Autowired
	UserRepository userRepository;

	private Sort.Direction getSortDirection(String direction) {
		if (direction.equals("ASC")) {
			return Sort.Direction.ASC;
		} else if (direction.equals("DESC")) {
			return Sort.Direction.DESC;
		}

		return Sort.Direction.ASC;
	}

	@Operation(summary = "Add an user group", description = "This can only be done by admin.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Groups" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Status OK") })
	@PostMapping("/groups")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Group> createGroup(@RequestBody CreateGroupRequest createGroupRequest) {
		try {
			String name = createGroupRequest.getName();
			Group _group = groupRepository.save(new Group(name));
			return new ResponseEntity<>(_group, HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Update a group name by ID", description = "This can only be done by admin.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Groups" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/groups/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Group> updateGroup(@PathVariable("id") long id, @RequestBody CreateGroupRequest createGroupRequest) {
		Optional<Group> groupData = groupRepository.findById(id);
		if (groupData.isPresent()) {
			Group _group = groupData.get();
			String name = createGroupRequest.getName();
			_group.setName(name);
			return new ResponseEntity<>(groupRepository.save(_group), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Get a group by ID", description = "This can only be done by logged in user.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Groups" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Group.class))),
			@ApiResponse(responseCode = "400", description = "Invalid ID supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "Group not found", content = @Content) })
	@GetMapping("/groups/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<Group> getGroupById(@PathVariable("id") long id) {
		Optional<Group> groupData = groupRepository.findById(id);
		if (groupData.isPresent()) {
			return new ResponseEntity<>(groupData.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Get a group by name", description = "This can only be done by logged in user.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Groups" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = Group.class))),
			@ApiResponse(responseCode = "400", description = "Invalid name supplied", content = @Content),
			@ApiResponse(responseCode = "404", description = "Group not found", content = @Content) })
	@GetMapping("/groups/find/{name}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<Group> getGroupByName(@PathVariable("name") String name) {
		Optional<Group> group = groupRepository.findByName(name);
		if (group != null) {
			return new ResponseEntity<>(group.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Add a user to a group", description = "This can only be done by admin.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Groups" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@PutMapping("/groups/{groupId}/users/{userName}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> addMember(@PathVariable("userName") String userName,
			@PathVariable("groupId") Long groupId) {
		Optional<Group> groupData = groupRepository.findById(groupId);
		if (groupData.isPresent()) {
			Group _group = groupData.get();
			User user = userRepository.findByUsername(userName).get();
			_group.getUsers().add(user);
			groupRepository.save(_group);
			return new ResponseEntity<>(_group, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Operation(summary = "Get all groups", description = "This can only be done by logged in user.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Groups" })
	@ApiResponses(value = @ApiResponse(description = "successful operation"))
	@GetMapping("/groups")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public ResponseEntity<List<Group>> getAllGroups(@RequestParam(required = false) String name,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int perPage,
			@RequestParam(defaultValue = "id,asc") String[] sort) {
		try {
			List<Order> orders = new ArrayList<Order>();

			if (sort[0].contains(",")) {
				// will sort more than 2 fields
				// sortOrder="field, direction"
				for (String sortOrder : sort) {
					String[] _sort = sortOrder.split(",");
					orders.add(new Order(getSortDirection(_sort[1]), _sort[0]));
				}
			} else {
				// sort=[field, direction]
				orders.add(new Order(getSortDirection(sort[1]), sort[0]));
			}

			List<Group> groups = new ArrayList<Group>();
			Pageable pagingSort = PageRequest.of(page, perPage, Sort.by(orders));

			Page<Group> pageTuts;
			if (name == null)
				pageTuts = groupRepository.findAll(pagingSort);
			else
				pageTuts = groupRepository.findByNameContaining(name, pagingSort);

			groups = pageTuts.getContent();

			if (groups.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			HttpHeaders responseHeaders = new HttpHeaders();
			long l = pageTuts.getTotalElements();
			String total = String.valueOf(l);
			responseHeaders.set("x-total-count", total);

			return new ResponseEntity<>(groups, responseHeaders, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Operation(summary = "Delete a group", description = "This can only be done by admin.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Groups" })
	@ApiResponses(value = { @ApiResponse(responseCode = "400", description = "Invalid group ID supplied"),
			@ApiResponse(responseCode = "404", description = "Group not found") })
	@DeleteMapping("/groups/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Group> deleteGroupById(@PathVariable("id") long id) {
		try {
			groupRepository.deleteById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
