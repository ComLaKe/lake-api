package com.ulake.api.security.services.impl;

import java.io.IOException;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ulake.api.constant.AclSourceType;
import com.ulake.api.constant.AclTargetType;
import com.ulake.api.constant.PermType;
import com.ulake.api.models.Acl;
import com.ulake.api.models.File;
import com.ulake.api.models.User;
import com.ulake.api.repository.AclRepository;
import com.ulake.api.repository.FileRepository;
import com.ulake.api.repository.UserRepository;
import com.ulake.api.security.services.FilesStorageService;
import com.ulake.api.security.services.LocalPermissionService;

@Service
public class FilesStorageServiceImpl implements FilesStorageService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FileRepository fileRepository;

	@Autowired
	private AclRepository aclRepository;

	@Autowired
	private LocalPermissionService permissionService;

	@Override
	public File store(MultipartFile file) throws IOException {
		// Find out who is the current logged in user
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		User fileOwner = userRepository.findByEmail(userDetails.getEmail());
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		String fileMimeType = file.getContentType();
		Long fileSize = file.getSize();
		byte[] fileData = file.getBytes();
		File fileInfo = new File(fileOwner, fileName, fileMimeType, fileSize, fileData);

		// Save File Metadata in our db;
		fileRepository.save(fileInfo);

		// Add ACL WRITE and READ Permission For Admin and File Owner
		permissionService.addPermissionForAuthority(fileInfo, BasePermission.READ, "ROLE_ADMIN");
		permissionService.addPermissionForAuthority(fileInfo, BasePermission.WRITE, "ROLE_ADMIN");
		permissionService.addPermissionForUser(fileInfo, BasePermission.READ, authentication.getName());
		permissionService.addPermissionForUser(fileInfo, BasePermission.WRITE, authentication.getName());

		aclRepository.save(
				new Acl(fileInfo.getId(), fileOwner.getId(), AclSourceType.FILE, AclTargetType.USER, PermType.READ));
		aclRepository.save(
				new Acl(fileInfo.getId(), fileOwner.getId(), AclSourceType.FILE, AclTargetType.USER, PermType.WRITE));

		return fileInfo;
	}

	@Override
	public Stream<File> getAllFiles() {
		return fileRepository.findAll().stream();
	}

}
