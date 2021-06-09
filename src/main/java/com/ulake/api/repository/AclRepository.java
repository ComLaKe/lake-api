package com.ulake.api.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ulake.api.constant.AclSourceType;
import com.ulake.api.constant.AclTargetType;
import com.ulake.api.models.Acl;

@Repository
@Transactional
public interface AclRepository extends JpaRepository<Acl, Long> {
//	@Modifying
//	@Query(value = "DELETE FROM clake_acls a WHERE a.source_id = :sourceId AND a.target_id = :targetId AND a.source_type = :sourceType AND a.target_type = :targetType", nativeQuery = true)
//	void removeBySourceIdAndTargetIdAndSourceTypeAndTargetType(@Param("sourceId") Long sourceId,
//			@Param("targetId") Long targetId, @Param("sourceType") AclSourceType sourceType,
//			@Param("targetType") AclTargetType targetType);
//
//	@Modifying
//	@Query(value = "DELETE FROM clake_acls a WHERE a.source_id = :sourceId AND a.source_type = :sourceType", nativeQuery = true)
//	void removeBySourceIdAndSourceType(@Param("sourceId") Long sourceId, @Param("sourceType") AclSourceType sourceType);

	void removeBySourceIdAndTargetIdAndSourceTypeAndTargetType(Long sourceId, Long targetId, AclSourceType sourceType,
			AclTargetType targetType);

	void removeBySourceIdAndSourceType(Long sourceId, AclSourceType sourceType);

	List<Acl> findByTargetTypeAndTargetId(AclTargetType targetType, Long targetId);

	List<Acl> findBySourceIdAndTargetIdAndSourceTypeAndTargetType(Long sourceId, Long targetId,
			AclSourceType sourceType, AclTargetType targetType);

}
