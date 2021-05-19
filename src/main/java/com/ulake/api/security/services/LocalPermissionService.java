package com.ulake.api.security.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.ulake.api.controllers.FileController;
import com.ulake.api.models.IEntity;

@Service
@Transactional
public class LocalPermissionService {

    @Autowired
    private MutableAclService aclService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Logger LOGGER = LoggerFactory.getLogger(LocalPermissionService.class);
    
    public void addPermissionForUser(IEntity targetObj, Permission permission, String username) {
        final Sid sid = new PrincipalSid(username);
        addPermissionForSid(targetObj, permission, sid);
        LOGGER.error("Grant {} permission to principal {} on Object {}", 
        		permission, username, targetObj);
    }
    
    public void updatePermissionForUser(IEntity targetObj, Permission permission, String username) {
        final Sid sid = new PrincipalSid(username);
        updatePermissionForSid(targetObj, permission, sid);
        LOGGER.error("Grant {} permission to principal {} on Object {}", 
        		permission, username, targetObj);
    }

    public void addPermissionForAuthority(IEntity targetObj, Permission permission, String authority) {
        final Sid sid = new GrantedAuthoritySid(authority);
        addPermissionForSid(targetObj, permission, sid);
    }

    private void addPermissionForSid(IEntity targetObj, Permission permission, Sid sid) {
        final TransactionTemplate tt = new TransactionTemplate(transactionManager);

        tt.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                final ObjectIdentity oi = new ObjectIdentityImpl(targetObj.getClass(), targetObj.getId());

                MutableAcl acl = null;
                try {
                    acl = (MutableAcl) aclService.readAclById(oi);
                } catch (final NotFoundException nfe) {
                    acl = aclService.createAcl(oi);
                }

                acl.insertAce(acl.getEntries().size(), permission, sid, true);
                LOGGER.debug("acl.getEntries().size(): ", acl.getEntries().size());
                
                aclService.updateAcl(acl);
                LOGGER.debug("ACL: ", acl);
            }
        });
    }
    
    private void updatePermissionForSid(IEntity targetObj, Permission permission, Sid sid) {
        final TransactionTemplate tt = new TransactionTemplate(transactionManager);

        tt.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                final ObjectIdentity oi = new ObjectIdentityImpl(targetObj.getClass(), targetObj.getId());

                MutableAcl acl = null;
                acl = (MutableAcl) aclService.readAclById(oi);
            	
                aclService.deleteAcl(oi, true);
                
                acl.insertAce(acl.getEntries().size(), permission, sid, true);
                LOGGER.debug("acl.getEntries().size(): ", acl.getEntries().size());
                
                aclService.updateAcl(acl);
                LOGGER.debug("ACL: ", acl);
            }
        });
    }
}