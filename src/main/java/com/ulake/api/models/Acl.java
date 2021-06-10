package com.ulake.api.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.ulake.api.constant.AclSourceType;
import com.ulake.api.constant.AclTargetType;
import com.ulake.api.constant.PermType;

@Entity
@Table(name = "CLake_acls")
public class Acl extends Auditable<String> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 36, nullable = false)
	private String sourceName;

	@Column(length = 36, nullable = false)
	private String targetName;

	@Enumerated(EnumType.STRING)
	@Column(length = 30, nullable = false)
	private PermType perm;

	@Enumerated(EnumType.STRING)
	@Column(length = 30, nullable = false)
	private AclTargetType targetType;

	@Enumerated(EnumType.STRING)
	@Column(length = 30, nullable = false)
	private AclSourceType sourceType;

	public Acl() {

	}

	public Acl(String sourceName, String targetName, AclSourceType sourceType, AclTargetType targetType, PermType perm) {
		this.sourceName = sourceName;
		this.targetName = targetName;
		this.sourceType = sourceType;
		this.targetType = targetType;
		this.perm = perm;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PermType getPerm() {
		return perm;
	}

	public void setPerm(PermType perm) {
		this.perm = perm;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public AclTargetType getTargetType() {
		return targetType;
	}

	public void setTargetType(AclTargetType targetType) {
		this.targetType = targetType;
	}
	
	public AclSourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(AclSourceType sourceType) {
		this.sourceType = sourceType;
	}

}
