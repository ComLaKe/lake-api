-- Bit mask 	
-- 1 read 
-- 2 write 
-- 4 create 
-- 8 delete 
-- 16 admin
INSERT INTO acl_sid (id, principal, sid) VALUES
(1, 0, 'ROLE_ADMIN'),
(2, 0, 'ROLE_USER');

INSERT INTO acl_class (id, class) VALUES
(1, 'com.ulake.api.models.File'),
(2, 'com.ulake.api.models.Folder');
