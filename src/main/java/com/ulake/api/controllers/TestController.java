package com.ulake.api.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {
	@Operation(summary = "Has Role User", description = "This can only by done by User.", tags = {
			"Internal - Debugging Tools" })
	@GetMapping("/all")
	public String allAccess() {
		return "Public Content.";
	}

	@Operation(summary = "Has Role User", description = "This can only by done by User.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Internal - Debugging Tools" })
	@GetMapping("/user")
	@PreAuthorize("hasRole('USER')")
	public String userAccess() {
		return "User Content.";
	}

	@Operation(summary = "Has Role Admin", description = "This can only by done by Admin.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "Internal - Debugging Tools" })
	@GetMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public String adminAccess() {
		return "Admin Board.";
	}

}