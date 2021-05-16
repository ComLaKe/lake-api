-- Bit mask 	
-- 1 read 
-- 2 write 
-- 4 create 
-- 8 delete 
-- 16 admin
INSERT INTO acl_sid (id, principal, sid) VALUES
(1, 0, 'ROLE_ADMIN'),
(2, 0, 'ROLE_USER'),
(3, 1, 'admin'),
(4, 1, 'user1');

INSERT INTO acl_class (id, class) VALUES
(1, 'com.ulake.api.models.Document');

INSERT INTO acl_object_identity (id, object_id_class, object_id_identity, parent_object, owner_sid, entries_inheriting) VALUES
(1, 1, 1, NULL, 1, 1), -- ROLE_ADMIN Document object identity
(2, 1, 2, NULL, 1, 1), -- Common Document object identity
(3, 1, 3, NULL, 2, 1); -- ROLE_USER Document object identity

INSERT INTO acl_entry (id, acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) VALUES
(1, 1, 0, 1, 16, 1, 0, 0), -- ROLE_ADMIN has Admin permission for ROLE_ADMIN Document
(2, 2, 0, 1, 16, 1, 0, 0), -- ROLE_ADMIN has Admin permission for Common Document
(3, 2, 1, 2, 1, 1, 0, 0),  -- ROLE_USER has Read permission for Common Document
(4, 3, 0, 2, 16, 1, 0, 0); -- ROLE_USER has Admin permission for ROLE_USER Document