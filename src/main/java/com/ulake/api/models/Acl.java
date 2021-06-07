package com.ulake.api.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.ulake.api.constant.AclTargetType;
import com.ulake.api.constant.PermType;

@Entity
@Table(name = "CLake_acls")
public class Acl extends Auditable<String>{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Column(length = 36, nullable = false)
    private Long sourceId;
    
    @Column(length = 36, nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private PermType perm;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private AclTargetType type;
    
    public Acl() {
    	
    }

    public Acl(Long sourceId, Long targetId, PermType perm, AclTargetType type) {
    	this.sourceId = sourceId;
    	this.targetId = targetId;
    	this.perm = perm; 
    	this.type = type;
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

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public AclTargetType getType() {
        return type;
    }

    public Acl setType(AclTargetType type) {
        this.type = type;
        return this;
    }

}
