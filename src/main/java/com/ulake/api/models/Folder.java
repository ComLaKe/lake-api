package com.ulake.api.models;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "CLake_folders")
public class Folder extends Auditable<String> implements IEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "creator_id", nullable = false)
	private User creator;

	@OneToOne
	@JoinColumn(name = "parent_id")
	@JsonIgnore
	private Folder parent;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "parent")
	private Set<Folder> subfolders = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "folder")
	private Set<File> files = new HashSet<>();

	private String cid;

	private String datasetId;

	private String name;
	
	private String type = "Folder";
	
    private Boolean isFirstNode = true;

	public Folder() {

	}

	public Folder(User creator, String name) {
		this.creator = creator;
		this.name = name;
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Folder getParent() {
		return parent;
	}

	public void setParent(Folder parent) {
		this.parent = parent;
	}

	public Set<Folder> getSubfolders() {
		return subfolders;
	}

	public void setSubfolders(Set<Folder> subfolders) {
		this.subfolders = subfolders;
	}

	public void addSubfolder(Folder subfolders) {
		this.subfolders.add(subfolders);
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getDatasetId() {
		return datasetId;
	}

	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Set<File> getFiles() {
		return files;
	}

	public void setFiles(Set<File> files) {
		this.files = files;
	}
	
	public Boolean getIsFirstNode() {
		return isFirstNode;
	}
	
	public void setIsFirstNode(Boolean isFirstNode) {
		this.isFirstNode = isFirstNode; 
	}
}
