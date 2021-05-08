package com.ulake.api.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotBlank;

@Entity
@Table(	name = "CLake_files")
public class File {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank
	@ManyToOne
	@JoinColumn(name = "document_id", nullable = false)
	private Document document;
	
	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	@NotBlank
	private User user; 
	
	private String cid;
	
	@NotBlank
	private String name;
	
	private String mimeType;
	
	@Temporal(TemporalType.TIMESTAMP)   
	@Column(name = "date_created", nullable = false, updatable = false, insertable = false, 
	columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createDate;

	@Temporal(TemporalType.TIMESTAMP)   
	@Column(name = "date_updated", nullable = false, updatable = false, insertable = false, 
	columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updateDate;
	
	public File() {
	}
	
	public File(Long id) {
		this.id = id;
	}
	
	public File(Long documentId, Long fileId) {
		this.id = fileId;
		this.document = new Document(documentId);
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Document getDocument() {
		return document;
	}
	
	public void setDocument(Document document) {
		this.document = document;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}	
	
	public Date getCreateDate() {
		return createDate;
	}
	
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	public Date getUpdateDate() {
		return updateDate;
	}
	
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
}
