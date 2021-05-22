package com.ulake.api.security.services.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ulake.api.controllers.FileController;
import com.ulake.api.models.File;
import com.ulake.api.repository.FileRepository;
import com.ulake.api.repository.GroupRepository;
import com.ulake.api.repository.UserRepository;
import com.ulake.api.security.services.FilesStorageService;
import com.ulake.api.security.services.LocalPermissionService;

@Service
public class FilesStorageServiceImpl implements FilesStorageService {
  private final Path root = Paths.get("uploads");
  
  private Logger LOGGER = LoggerFactory.getLogger(FilesStorageService.class);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private FileRepository fileRepository;

  @Autowired
  private LocalPermissionService permissionService;

  @Override
  public void init() {
    try {
      Files.createDirectory(root);
    } catch (IOException e) {
      throw new RuntimeException("Could not initialize folder for upload!");
    }
  }

  @Override
  public void save(MultipartFile file) {
    try {
      Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));
      
      File fileInfo = null;
      
      // Find out who is the current logged in user
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
      
      // Set File Owner
      fileInfo.setOwner(userRepository.findByEmail(userDetails.getEmail()));
	  fileInfo.setName(file.getOriginalFilename());
	  fileInfo.setMimeType(file.getContentType());
	  fileInfo.setSize(file.getSize());
	  
	  //	Save File Metadata in our db;
	  fileRepository.save(fileInfo);
      LOGGER.error("fileInfo", fileInfo);
	  //	Add ACL WRITE and READ Permission For Admin and File Owner
      permissionService.addPermissionForAuthority(fileInfo, BasePermission.READ, "ROLE_ADMIN");
      permissionService.addPermissionForAuthority(fileInfo, BasePermission.WRITE, "ROLE_ADMIN");
      permissionService.addPermissionForUser(fileInfo, BasePermission.READ, authentication.getName());
      permissionService.addPermissionForUser(fileInfo, BasePermission.WRITE, authentication.getName());	  
    } catch (Exception e) {
      throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
    }
  }

  @Override
  public Resource load(String filename) {
    try {
      Path file = root.resolve(filename);
      Resource resource = new UrlResource(file.toUri());

      if (resource.exists() || resource.isReadable()) {
        return resource;
      } else {
        throw new RuntimeException("Could not read the file!");
      }
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error: " + e.getMessage());
    }
  }

  @Override
  public void deleteAll() {
    FileSystemUtils.deleteRecursively(root.toFile());
  }

  @Override
  public Stream<Path> loadAll() {
    try {
      return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
    } catch (IOException e) {
      throw new RuntimeException("Could not load the files!");
    }
  }
}
