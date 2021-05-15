package com.ulake.api.security.services;

import java.util.List;

import com.ulake.api.models.Document;
import com.ulake.api.models.File;

public interface DocumentService {

	List<Document> getDocuments();
	
	Document getDocument(long id, boolean initFiles);
	
	void createFile(File file);

	File getFile(long fileId);
	
//	void setFileName(File file);
	
	void setFileVisible(File file);
	
	void deleteFile(File file);
}
