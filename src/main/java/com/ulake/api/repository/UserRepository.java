package com.ulake.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ulake.api.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);

	User findByEmail(String email);

	Page<User> findByEmailContaining(String email, Pageable pageable);

	Page<User> findByFirstname(String firstname, Pageable pageable);

	Page<User> findByLastname(String lastname, Pageable pageable);

	Page<User> findByDepartment(String department, Pageable pageable);

	Page<User> findByAffiliation(String affiliation, Pageable pageable);

	Page<User> findByUsername(String title, Pageable pageable);

	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);
}