package com.ulake.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ulake.api.models.Group;
import com.ulake.api.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);

	User findByEmail(String email);

	List<User> findByUsernameContaining(String username);
	
	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);
}