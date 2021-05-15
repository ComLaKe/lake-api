package com.ulake.api.dao;

import java.io.Serializable;

import com.ulake.api.models.Document;

public interface DocumentDao extends Dao<Document> {
	Document get(Serializable id, boolean initMessages);
}
