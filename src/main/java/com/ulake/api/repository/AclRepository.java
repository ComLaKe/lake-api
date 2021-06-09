package com.ulake.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ulake.api.constant.AclSourceType;
import com.ulake.api.constant.AclTargetType;
import com.ulake.api.models.Acl;

public interface AclRepository extends JpaRepository<Acl, Long>{
	void deleteAllBySourceIdAndSourceType(Long sourceId, AclSourceType sourceType);
	
	void deleteBySourceIdAndSourceType(Long sourceId, AclSourceType sourceType);
	
	List<Acl> findByTargetTypeAndTargetId(AclTargetType targetType, Long targetId);
}
