package com.ulake.api.security.services;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ulake.api.dao.DocumentDao;
import com.ulake.api.dao.FileDao;
import com.ulake.api.models.Document;
import com.ulake.api.models.File;
import com.ulake.api.security.services.FileFilter;

@Service
@Transactional
@PreAuthorize("denyAll")
public class DocumentServiceImpl implements DocumentService {
	private static Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);
	
	@Inject private DocumentDao documentDao;
	@Inject private FileDao fileDao;
	@Inject private MutableAclService aclService;
	@Inject private FileFilter fileFilter;
	
	@Override
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@PostFilter("hasPermission(filterObject, read)")
	public List<Document> getDocuments() {
		return documentDao.getAll();
	}
	
	@Override
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@PostAuthorize("hasPermission(returnObject, read)")
	public Document getDocument(long id, boolean initFiles) {
		Document document = documentDao.get(id, initFiles);
		document.setFiles(fileFilter.filter(document.getFiles()));
		return document;
	}
	
	@Override
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	public void createFile(File file) {
		fileDao.create(file);
		createAcl(file);
	}
	
	@Override
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
	@PostAuthorize("(hasPermission(returnObject, read) and returnObject.visible) or hasPermission(returnObject, admin)")
	public File getFile(long id) {
		return fileDao.get(id);
	}
	
//	@Override
//	@PreAuthorize("(hasPermission(#file, write) and #file.visible) or hasPermission(#file, admin)")
//	public void setFileName(File file) {
//		File pFile = fileDao.get(file.getId());
//		pFile.setName(file.getName());
//		fileDao.update(pFile);
//		updateAcl(pFile);
//	}
	
	@Override
	@PreAuthorize("hasPermission(#file, admin)")
	public void setFileVisible(File file) {
		File pFile = fileDao.get(file.getId());
		pFile.setVisible(file.isVisible());
		fileDao.update(pFile);
		updateAcl(pFile);
	}
	
	@Override
	@PreAuthorize("hasPermission(#file, delete)")
	public void deleteFile(File file) {
		fileDao.delete(file);
		deleteAcl(file);
	}
	
	
	// =================================================================================================================
	// ACL helper methods
	// =================================================================================================================
	
	private void createAcl(File file) {
		ObjectIdentity parentOid = new ObjectIdentityImpl(Document.class, file.getDocument().getId());
		log.debug("Loading ACL for document OID: {}", parentOid);
		MutableAcl parentAcl = (MutableAcl) aclService.readAclById(parentOid);
		log.debug("Loaded document ACL: {}", parentAcl);
		
		ObjectIdentity oid = new ObjectIdentityImpl(File.class, file.getId());
		log.debug("Creating ACL for file OID: {}", oid);
		
		// This automatically makes the current principal the owner, at least with
		// the JDBC implementation.
		MutableAcl acl = aclService.createAcl(oid);
		
		Sid author = new PrincipalSid(file.getUser().getUsername());
		log.debug("Setting file owner: {}", author);
		
		// Checks against AclAuthorizationStrategy.CHANGE_GENERAL. This check
		// passes because the current principal is the ACL owner.
		acl.setParent(parentAcl);
		
		if (file.isVisible()) {
			// Checks against AclAuthorizationStrategy.CHANGE_GENERAL. Again
			// this check passes because the current principal is the ACL owner.
			acl.insertAce(0, BasePermission.WRITE, author, true);
		}
		
		// Checks against AclAuthorizationStrategy.CHANGE_OWNERSHIP. This
		// passes because the current principal is the ACL owner, but it won't
		// be after this call is done. Therefore do this last since we need to
		// own the ACL in order to change the parent and add the ACE.
		acl.setOwner(author);
		
		aclService.updateAcl(acl);
	}

	private void deleteAcl(File file) {
		ObjectIdentity oid = new ObjectIdentityImpl(File.class, file.getId());
		aclService.deleteAcl(oid, true);
	}

	private void updateAcl(File file) {
		deleteAcl(file);
		createAcl(file);
	}
}
