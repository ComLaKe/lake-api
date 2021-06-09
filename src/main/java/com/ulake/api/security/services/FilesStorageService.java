package com.ulake.api.security.services;

import java.io.IOException;
import java.util.stream.Stream;

import org.springframework.web.multipart.MultipartFile;

import com.ulake.api.models.File;

public interface FilesStorageService {
	public Stream<File> getAllFiles();

	File store(MultipartFile file) throws IOException;
}
