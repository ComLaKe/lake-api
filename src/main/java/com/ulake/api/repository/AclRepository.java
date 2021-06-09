package com.ulake.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ulake.api.constant.AclSourceType;
import com.ulake.api.constant.AclTargetType;
import com.ulake.api.models.Acl;

@Repository
public interface AclRepository extends JpaRepository<Acl, Long> {
	void removeBySourceNameAndTargetNameAndSourceTypeAndTargetType(String sourceName, String targetName, AclSourceType sourceType,
			AclTargetType targetType);

	void removeBySourceNameAndSourceType(String sourceName, AclSourceType sourceType);

	List<Acl> findByTargetTypeAndTargetName(AclTargetType targetType, String targetName);

	List<Acl> findBySourceNameAndTargetNameAndSourceTypeAndTargetType(String sourceName, String targetName,
			AclSourceType sourceType, AclTargetType targetType);
}
