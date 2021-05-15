package com.ulake.api.security.services;

import java.util.List;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Component;

import com.ulake.api.models.File;

@Component
public class FileFilter {
	@PostFilter("(hasPermission(filterObject, read) and filterObject.visible) or hasPermission(filterObject, admin)")
	public List<File> filter(List<File> files) { return files; }
}
