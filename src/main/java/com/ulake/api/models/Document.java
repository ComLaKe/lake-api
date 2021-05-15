package com.ulake.api.models;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;

@Entity
@Table(	name = "CLake_documents")
@NamedQuery(
		name = "getDocumentsWithStats",
		query = "select Document, count(file), max(file.createDate)" +
			" from Document as Document" +
			" left outer join Document.files as file with file.visible = true" +
			" group by Document")
public class Document {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank
	private String title;

	@ManyToOne
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;
		
	@OneToMany(mappedBy = "document")
	private List<File> files = new ArrayList<File>();
		
    private String language;

    private String description;

    private String topics;

    private String source;
    
	@Temporal(TemporalType.TIMESTAMP)   
	@Column(name = "date_created", nullable = false, updatable = false, insertable = false, 
	columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createDate;

	@Temporal(TemporalType.TIMESTAMP)   
	@Column(name = "date_updated", nullable = false, updatable = false, insertable = false, 
	columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updateDate;
	
	// Stats fields
	private boolean calculateFileStats = true;
	private int numVisibleFiles;
	private Date lastVisibleFileDate;

	public Document() {
		
	}
	
	public Document(Long id) {
		this.id = id;
	}
	public Document(Document orig) {
		this.id = orig.id;
		this.title = orig.title;
		this.owner = orig.owner;
		this.files = orig.files;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public User getOwner() {
		return owner;
	}
	
	public void setOwner(User owner) {
		this.owner = owner;
	}
	
	public List<File> getFiles(){
		return files;
	}
	
	public void setFiles(List<File> files) {
		this.files = files;
	}
	
	public void clearFiles() {
		files.clear();
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}	
	
	public String getTopics() {
		return topics;
	}
	
	public void setTopics(String topics) {
		this.topics = topics;
	}

	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
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
	
	@Transient
	public boolean getCalculateFileStats() { return calculateFileStats; }
	
	public void setCalculateFileStats(boolean flag) {
		this.calculateFileStats = flag;
	}

	@Transient
	public int getNumVisibleFiles() {
		if (calculateFileStats) {
			int count = 0;
			for (File file : files) {
				if (file.isVisible()) { count++; }
			}
			return count;
		} else {
			return numVisibleFiles;
		}
	}
	
	public void setNumVisibleFiles(int n) {
		this.numVisibleFiles = n;
	}

	@Transient
	public Date getLastVisibleFileDate() {
		if (calculateFileStats) {
			Date date = null;
			for (File file : files) {
				if (file.isVisible()) {
					Date dateCreated = file.getCreateDate();
					if (date == null || date.compareTo(dateCreated) < 0) {
						date = file.getCreateDate();
					}
				}
			}
			return date;
		} else {
			return lastVisibleFileDate;
		}
	}
	
	public void setLastVisibleFileDate(Date date) {
		this.lastVisibleFileDate = date;
	}
}
