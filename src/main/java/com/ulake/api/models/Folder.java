package com.ulake.api.models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(	name = "CLake_folders")
public class Folder implements IEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner; 
	
	@OneToMany(mappedBy = "folder")
	private List<File> files = new ArrayList<File>();
	
	private String cid;

	private String name;

	private String mimeType;

	private String source;
	
	private String topics;
	
	private Long size;
	
	public Folder() {
		
	}
	
    @Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public User getOwner() {
		return owner;
	}
	
	public void setOwner(User owner) {
		this.owner = owner;
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
	
	public List<File> getFiles() { return files; }
	
	public void setFiles(List<File> files) { this.files = files; }
	
	public void clearFiles() { files.clear(); }


}
