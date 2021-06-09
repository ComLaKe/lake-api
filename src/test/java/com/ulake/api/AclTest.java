package com.ulake.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ulake.api.constant.AclSourceType;
import com.ulake.api.constant.AclTargetType;
import com.ulake.api.constant.PermType;
import com.ulake.api.models.Acl;
import com.ulake.api.models.File;
import com.ulake.api.models.User;
import com.ulake.api.repository.AclRepository;
import com.ulake.api.repository.FileRepository;
import com.ulake.api.repository.FolderRepository;
import com.ulake.api.repository.GroupRepository;
import com.ulake.api.repository.UserRepository;

@SpringBootTest
public class AclTest {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FileRepository fileRepository;

	@Autowired
	private FolderRepository folderRepository;

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private AclRepository aclRepository;
	
	@Test
	@DisplayName("Should Delete a ACL")
	public void shouldDeleteAcl() {
		long fileId = 7;
		long userId = 1;
//		aclRepository.deleteBySourceIdAndTargetIdAndSourceTypeAndTargetType(fileId, userId, AclSourceType.FILE,
//				AclTargetType.USER);
        assertEquals(1, aclRepository.findBySourceIdAndTargetIdAndSourceTypeAndTargetType(fileId, userId, AclSourceType.FILE,
				AclTargetType.USER));
	}
	
}
