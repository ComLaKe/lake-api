package com.ulake.api.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ulake.api.constant.AclSourceType;
import com.ulake.api.constant.AclTargetType;
import com.ulake.api.constant.PermType;
import com.ulake.api.models.Acl;

@Repository
@Transactional
public interface AclRepository extends JpaRepository<Acl, Long> {
	void removeBySourceIdAndTargetNameAndSourceTypeAndTargetType(Long sourceId, String targetName, AclSourceType sourceType,
			AclTargetType targetType);

	void removeBySourceIdAndTargetNameAndSourceTypeAndTargetTypeAndPerm(Long sourceId, String targetName,
			AclSourceType sourceType, AclTargetType targetType, PermType perm);

	void removeBySourceIdAndSourceType(Long sourceId, AclSourceType sourceType);

	List<Acl> findByTargetTypeAndTargetName(AclTargetType targetType, Long targetName);

	List<Acl> findBySourceTypeAndSourceId(AclSourceType sourceType, Long sourceId);

	List<Acl> findBySourceIdAndTargetNameAndSourceTypeAndTargetType(Long sourceId, String targetName,
			AclSourceType sourceType, AclTargetType targetType);
}