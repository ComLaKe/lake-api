use ulakedb;

-- ============================================================================
-- ACL tables
-- ============================================================================
drop table if exists acl_class;
create table acl_class (
    id smallint unsigned not null auto_increment primary key,
    class varchar(100) unique not null
) engine = InnoDb;

drop table if exists acl_sid;
create table acl_sid (
    id int unsigned not null auto_increment primary key,
    principal boolean not null,
    sid varchar(100) not null,
    unique index acl_sid_idx_1 (sid, principal)
) engine = InnoDb;
drop table if exists acl_object_identity;
create table acl_object_identity (
    id int unsigned not null auto_increment primary key,
    object_id_class smallint unsigned not null,
    object_id_identity int unsigned not null,
    parent_object int unsigned,
    owner_sid int unsigned,
    entries_inheriting boolean not null,
    unique index acl_object_identity_idx_1 (object_id_class, object_id_identity),
    foreign key (object_id_class) references acl_class (id),
    foreign key (parent_object) references acl_object_identity (id),
    foreign key (owner_sid) references acl_sid (id)
) engine = InnoDb;
drop table if exists acl_entry;
create table acl_entry (
    id int unsigned not null auto_increment primary key,
    acl_object_identity int unsigned not null,
    ace_order int unsigned not null,
    sid int unsigned not null,
    mask int not null,
    granting boolean not null default 1,
    audit_success boolean not null default 0,
    audit_failure boolean not null default 0,
    unique index acl_entry_idx_1 (acl_object_identity, ace_order),
    foreign key (acl_object_identity) references acl_object_identity (id),
    foreign key (sid) references acl_sid (id)
) engine = InnoDb;