package com.ulake.api.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ulake.api.constant.ERole;
import com.ulake.api.exception.TokenRefreshException;
import com.ulake.api.models.RefreshToken;
import com.ulake.api.models.Role;
import com.ulake.api.models.User;
import com.ulake.api.payload.request.LoginRequest;
import com.ulake.api.payload.request.SignupRequest;
import com.ulake.api.payload.request.TokenRefreshRequest;
import com.ulake.api.payload.response.JwtResponse;
import com.ulake.api.payload.response.MessageResponse;
import com.ulake.api.payload.response.TokenRefreshResponse;
import com.ulake.api.repository.RoleRepository;
import com.ulake.api.repository.UserRepository;
import com.ulake.api.security.jwt.JwtUtils;
import com.ulake.api.security.services.RefreshTokenService;
import com.ulake.api.security.services.impl.UserDetailsImpl;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	RefreshTokenService refreshTokenService;

	@Value("${app.jwtExpirationMs}")
	Long jwtExpirationMs;

	@Operation(summary = "Logs user into the system", tags = { "User Authentication" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Invalid username/password supplied", content = @Content) })
	@PostMapping("/api/auth/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		String jwt = jwtUtils.generateJwtToken(userDetails);

		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
				.collect(Collectors.toList());

		RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

		Long jwtExpirationS = jwtExpirationMs / 1000;

		return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), jwtExpirationS, userDetails.getId(),
				userDetails.getUsername(), userDetails.getEmail(), roles));
	}

	@Operation(summary = "Create user", description = "Create user.", tags = { "User Authentication" })
	@ApiResponses(value = { @ApiResponse(description = "successful operation", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) }) })
	@PostMapping("/api/users")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
		}

		// Create new user's account
		User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
				encoder.encode(signUpRequest.getPassword()), signUpRequest.getFirstname(), signUpRequest.getLastname(),
				signUpRequest.getDepartment(), signUpRequest.getAffiliation());

		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			Role userRole = roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				switch (role) {
				case "admin":
					Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(adminRole);

					break;
				default:
					Role userRole = roleRepository.findByName(ERole.ROLE_USER)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		userRepository.save(user);
		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}

	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@Operation(summary = "Refresh Token", description = "Refresh Token.", tags = { "User Authentication" })
	@PostMapping("/api/auth/refresh-token")
	public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
		String requestRefreshToken = request.getRefreshToken();

		return refreshTokenService.findByToken(requestRefreshToken).map(refreshTokenService::verifyExpiration)
				.map(RefreshToken::getUser).map(user -> {
					String token = jwtUtils.generateTokenFromUsername(user.getUsername());
					return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
				})
				.orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
	}

	@Operation(summary = "Logout current user", description = "And delete Refresh Token in database.", security = {
			@SecurityRequirement(name = "bearer-key") }, tags = { "User Authentication" })
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@GetMapping("/logout")
	public ResponseEntity<?> logoutUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		long userId = userDetails.getId();
		refreshTokenService.deleteByUserId(userId);
		return ResponseEntity.ok(new MessageResponse("Log out successful!"));
	}
}