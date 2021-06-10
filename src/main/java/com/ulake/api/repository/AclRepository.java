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
	void removeBySourceIdAndTargetIdAndSourceTypeAndTargetType(Long sourceId, Long targetId, AclSourceType sourceType,
			AclTargetType targetType);

	void removeBySourceIdAndTargetIdAndSourceTypeAndTargetTypeAndPerm(Long sourceId, Long targetId,
			AclSourceType sourceType, AclTargetType targetType, PermType perm);

	void removeBySourceIdAndSourceType(Long sourceId, AclSourceType sourceType);

	List<Acl> findByTargetTypeAndTargetId(AclTargetType targetType, Long targetId);

	List<Acl> findBySourceTypeAndSourceId(AclSourceType sourceType, Long sourceId);

	List<Acl> findBySourceIdAndTargetIdAndSourceTypeAndTargetType(Long sourceId, Long targetId,
			AclSourceType sourceType, AclTargetType targetType);

}