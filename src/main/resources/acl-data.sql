INSERT INTO acl_sid (id, principal, sid) VALUES
(1, 0, 'ROLE_ADMIN'),
(2, 0, 'ROLE_USER'),
(3, 1, 'admin'),
(4, 1, 'user1');

INSERT INTO acl_class (id, class) VALUES
(1, 'com.ulake.api.models.Document');
INSERT INTO acl_class (id, class) VALUES
(2, 'com.ulake.api.models.File');