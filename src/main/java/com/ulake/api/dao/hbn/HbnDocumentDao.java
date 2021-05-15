package com.ulake.api.dao.hbn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import org.springframework.stereotype.Repository;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import com.ulake.api.dao.DocumentDao;
import com.ulake.api.models.Document;

@Repository
public class HbnDocumentDao extends AbstractHbnDao<Document> implements DocumentDao {
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Document> getAll() {
		Session session = getSession();
		Query query = session.getNamedQuery("getDocumentsWithStats");
		List<Object[]> results = query.list();
		
		List<Document> documents = new ArrayList<Document>();
		for (Object[] result : results) {
			Document document = (Document) result[0];
			document.setCalculateFileStats(false);
			document.setNumVisibleFiles(NumberUtils.asInt((Long) result[1]));
			document.setLastVisibleFileDate((Date) result[2]);
			documents.add(document);
		}
		
		return documents;
	}

	public Document get(Serializable id, boolean initFiles) {
		Document document = get(id);
		if (initFiles) {
			Hibernate.initialize(document.getFiles());
		}
		return document;
	}
}
