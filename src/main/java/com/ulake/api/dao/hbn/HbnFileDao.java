package com.ulake.api.dao.hbn;

import org.springframework.stereotype.Repository;

import com.ulake.api.dao.FileDao;
import com.ulake.api.models.File;

@Repository
public class HbnFileDao extends AbstractHbnDao<File> implements FileDao { }
