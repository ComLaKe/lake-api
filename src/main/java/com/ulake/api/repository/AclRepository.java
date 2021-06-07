package com.ulake.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ulake.api.models.Acl;

public interface AclRepository extends JpaRepository<Acl, Long>{

}
