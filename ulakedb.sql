-- MySQL dump 10.13  Distrib 8.0.23, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: ulakedb
-- ------------------------------------------------------
-- Server version	8.0.23

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

DROP DATABASE IF EXISTS ulakedb;
CREATE DATABASE ulakedb;
USE ulakedb;
--
-- Table structure for table `acl_class`
--

DROP TABLE IF EXISTS `acl_class`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `acl_class` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `class` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_acl_class` (`class`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `acl_class`
--

LOCK TABLES `acl_class` WRITE;
/*!40000 ALTER TABLE `acl_class` DISABLE KEYS */;
INSERT INTO `acl_class` VALUES (1,'com.ulake.api.models.File');
/*!40000 ALTER TABLE `acl_class` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `acl_entry`
--

DROP TABLE IF EXISTS `acl_entry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `acl_entry` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `acl_object_identity` bigint unsigned NOT NULL,
  `ace_order` int NOT NULL,
  `sid` bigint unsigned NOT NULL,
  `mask` int unsigned NOT NULL,
  `granting` tinyint(1) NOT NULL,
  `audit_success` tinyint(1) NOT NULL,
  `audit_failure` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_acl_entry` (`acl_object_identity`,`ace_order`),
  KEY `fk_acl_entry_acl` (`sid`),
  CONSTRAINT `fk_acl_entry_acl` FOREIGN KEY (`sid`) REFERENCES `acl_sid` (`id`),
  CONSTRAINT `fk_acl_entry_object` FOREIGN KEY (`acl_object_identity`) REFERENCES `acl_object_identity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `acl_entry`
--

LOCK TABLES `acl_entry` WRITE;
/*!40000 ALTER TABLE `acl_entry` DISABLE KEYS */;
/*!40000 ALTER TABLE `acl_entry` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `acl_object_identity`
--

DROP TABLE IF EXISTS `acl_object_identity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `acl_object_identity` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `object_id_class` bigint unsigned NOT NULL,
  `object_id_identity` bigint NOT NULL,
  `parent_object` bigint unsigned DEFAULT NULL,
  `owner_sid` bigint unsigned DEFAULT NULL,
  `entries_inheriting` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_acl_object_identity` (`object_id_class`,`object_id_identity`),
  KEY `fk_acl_object_identity_parent` (`parent_object`),
  KEY `fk_acl_object_identity_owner` (`owner_sid`),
  CONSTRAINT `fk_acl_object_identity_class` FOREIGN KEY (`object_id_class`) REFERENCES `acl_class` (`id`),
  CONSTRAINT `fk_acl_object_identity_owner` FOREIGN KEY (`owner_sid`) REFERENCES `acl_sid` (`id`),
  CONSTRAINT `fk_acl_object_identity_parent` FOREIGN KEY (`parent_object`) REFERENCES `acl_object_identity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `acl_object_identity`
--

LOCK TABLES `acl_object_identity` WRITE;
/*!40000 ALTER TABLE `acl_object_identity` DISABLE KEYS */;
/*!40000 ALTER TABLE `acl_object_identity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `acl_sid`
--

DROP TABLE IF EXISTS `acl_sid`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `acl_sid` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `principal` tinyint(1) NOT NULL,
  `sid` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_acl_sid` (`sid`,`principal`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `acl_sid`
--

LOCK TABLES `acl_sid` WRITE;
/*!40000 ALTER TABLE `acl_sid` DISABLE KEYS */;
INSERT INTO `acl_sid` VALUES (3,1,'admin'),(1,0,'ROLE_ADMIN'),(2,0,'ROLE_USER'),(4,1,'user1');
/*!40000 ALTER TABLE `acl_sid` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clake_files`
--

DROP TABLE IF EXISTS `clake_files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clake_files` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cid` varchar(255) DEFAULT NULL,
  `date_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `mime_type` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `size` bigint DEFAULT NULL,
  `date_updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `owner_id` bigint NOT NULL,
  `source` varchar(255) DEFAULT NULL,
  `topics` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKayn9avfhg0wsxx1udktc4kdh4` (`owner_id`),
  CONSTRAINT `FKayn9avfhg0wsxx1udktc4kdh4` FOREIGN KEY (`owner_id`) REFERENCES `clake_users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clake_files`
--

LOCK TABLES `clake_files` WRITE;
/*!40000 ALTER TABLE `clake_files` DISABLE KEYS */;
/*!40000 ALTER TABLE `clake_files` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clake_groups`
--

DROP TABLE IF EXISTS `clake_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clake_groups` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqnpptu6jrjv37lko2mabqm54f` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clake_groups`
--

LOCK TABLES `clake_groups` WRITE;
/*!40000 ALTER TABLE `clake_groups` DISABLE KEYS */;
INSERT INTO `clake_groups` VALUES (2,'admin'),(3,'demo'),(1,'guest');
/*!40000 ALTER TABLE `clake_groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clake_refreshtoken`
--

DROP TABLE IF EXISTS `clake_refreshtoken`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clake_refreshtoken` (
  `id` bigint NOT NULL,
  `expiry_date` datetime NOT NULL,
  `token` varchar(255) NOT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_1d86p4vsimqa8q1l7fw5v3npt` (`token`),
  KEY `FK6lyx4l33f3e2v7o9ddsuk5e58` (`user_id`),
  CONSTRAINT `FK6lyx4l33f3e2v7o9ddsuk5e58` FOREIGN KEY (`user_id`) REFERENCES `clake_users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clake_refreshtoken`
--

LOCK TABLES `clake_refreshtoken` WRITE;
/*!40000 ALTER TABLE `clake_refreshtoken` DISABLE KEYS */;
INSERT INTO `clake_refreshtoken` VALUES (2,'2021-04-29 12:25:19','ad4167b6-8297-47cc-a75d-327a04cd4483',1),(3,'2021-04-30 15:42:45','30ddef31-23fe-441d-b01d-e40c8187cce7',1),(4,'2021-04-30 17:08:54','7d0d842e-16af-42ca-964c-408d9918f96d',2),(5,'2021-05-05 01:59:21','6e4e4e97-2212-461d-8672-7390f9cf0a96',1),(6,'2021-05-05 14:55:06','5e9f4eea-adf7-4542-a17c-5b7b4ec193db',1),(7,'2021-05-17 01:35:29','fd45c28b-304e-4805-bc9c-a57adcd42db8',1),(8,'2021-05-17 04:53:57','f957e39a-c1f2-4e2b-ba45-819294842b4e',1),(9,'2021-05-18 03:29:48','0b02291f-1b44-44c5-b97e-81413b918149',2),(10,'2021-05-18 10:41:36','fa36ba14-4708-464f-9236-de3296c7f954',1),(11,'2021-05-21 00:41:51','8b47005c-7d25-449c-a7e3-2e60349f6106',1),(12,'2021-05-21 00:42:44','236784d4-60f9-466d-b03a-f9562133e4b8',2),(14,'2021-05-22 01:22:11','52df81f6-0543-4b7c-8421-2863252686ef',1),(15,'2021-05-22 01:23:18','8494a9e3-dc05-47e2-b882-7a01e6dbbc3f',2),(16,'2021-05-22 01:57:19','e82df965-ed70-4aff-9008-fb2a0d3ad861',3);
/*!40000 ALTER TABLE `clake_refreshtoken` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clake_roles`
--

DROP TABLE IF EXISTS `clake_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clake_roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clake_roles`
--

LOCK TABLES `clake_roles` WRITE;
/*!40000 ALTER TABLE `clake_roles` DISABLE KEYS */;
INSERT INTO `clake_roles` VALUES (1,'ROLE_USER'),(2,'ROLE_ADMIN');
/*!40000 ALTER TABLE `clake_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clake_user_groups`
--

DROP TABLE IF EXISTS `clake_user_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clake_user_groups` (
  `group_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`group_id`,`user_id`),
  KEY `FKcnmx6od86ht9nkl20e69muysh` (`user_id`),
  CONSTRAINT `FKcnmx6od86ht9nkl20e69muysh` FOREIGN KEY (`user_id`) REFERENCES `clake_users` (`id`),
  CONSTRAINT `FKk4j4ep58hxtn8q82wtlbgsalj` FOREIGN KEY (`group_id`) REFERENCES `clake_groups` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clake_user_groups`
--

LOCK TABLES `clake_user_groups` WRITE;
/*!40000 ALTER TABLE `clake_user_groups` DISABLE KEYS */;
INSERT INTO `clake_user_groups` VALUES (1,2),(1,3),(3,4),(3,6);
/*!40000 ALTER TABLE `clake_user_groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clake_user_roles`
--

DROP TABLE IF EXISTS `clake_user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clake_user_roles` (
  `user_id` bigint NOT NULL,
  `role_id` int NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `FK5s7cnhsjdmeyt4jtdcw3e2a14` (`role_id`),
  CONSTRAINT `FK5s7cnhsjdmeyt4jtdcw3e2a14` FOREIGN KEY (`role_id`) REFERENCES `clake_roles` (`id`),
  CONSTRAINT `FKpwfrsypup4e5rmi7jw2fobpjl` FOREIGN KEY (`user_id`) REFERENCES `clake_users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clake_user_roles`
--

LOCK TABLES `clake_user_roles` WRITE;
/*!40000 ALTER TABLE `clake_user_roles` DISABLE KEYS */;
INSERT INTO `clake_user_roles` VALUES (2,1),(3,1),(4,1),(6,1),(7,1),(1,2);
/*!40000 ALTER TABLE `clake_user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clake_users`
--

DROP TABLE IF EXISTS `clake_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clake_users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(50) DEFAULT NULL,
  `password` varchar(120) DEFAULT NULL,
  `username` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5s5vpc7anjwqaaipqrfk2boru` (`username`),
  UNIQUE KEY `UKoj9gt4hdom76kdn6m2dr785yl` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clake_users`
--

LOCK TABLES `clake_users` WRITE;
/*!40000 ALTER TABLE `clake_users` DISABLE KEYS */;
INSERT INTO `clake_users` VALUES (1,'admin@example.com','$2a$10$PH9m3qCDxp5H4Pgn0e4oBeQWiriB76v25P0oYfdre0mD4qTQVR3Z6','admin'),(2,'user1@example.com','$2a$10$/F4d1t7nTV0sRjJ6PN4WhuzJzL/LjBBtkBodx.slCCv7aXsVMSDe6','user1'),(3,'user2@example.com','$2a$10$SsjIBK0AvMUraMMeBs9yBeDqXg9dvUMgd..dgy2SYuAeMhVfZnAxu','user2'),(4,'user3@example.com','$2a$10$Gi3fCEoAsFRhTqe19A3NKeYR8MCflssn6Lft5BeHNKc867u/BhJC.','user3'),(6,'user4@example.com','$2a$10$qk4VAIeIs8Md9pThuVUYPu2gOeUq3x5WtPAupqBX2Jh.DA7ecdJJO','user4'),(7,'user7@example.com','$2a$10$bAV2Qcn5rBQulGUeMF8fzeCo1RhJ11OSzvGF.u5gVI3UY2QBd/GFm','user5');
/*!40000 ALTER TABLE `clake_users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hibernate_sequence`
--

DROP TABLE IF EXISTS `hibernate_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hibernate_sequence`
--

LOCK TABLES `hibernate_sequence` WRITE;
/*!40000 ALTER TABLE `hibernate_sequence` DISABLE KEYS */;
INSERT INTO `hibernate_sequence` VALUES (17);
/*!40000 ALTER TABLE `hibernate_sequence` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-05-22 12:15:35
