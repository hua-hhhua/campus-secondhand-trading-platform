-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: campus_trade
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `article`
--

DROP TABLE IF EXISTS `article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `title` varchar(200) NOT NULL COMMENT '文章标题',
  `content` text NOT NULL COMMENT '文章内容',
  `user_id` int NOT NULL COMMENT '发布者ID（关联users表）',
  `view_count` int DEFAULT '0' COMMENT '浏览次数',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-草稿，1-已发布）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `published_at` datetime DEFAULT NULL COMMENT '发布时间',
  `is_top` int DEFAULT '0' COMMENT '是否置顶 0-否 1-是',
  `allow_comment` int DEFAULT '1' COMMENT '是否允许评论 0-不允许 1-允许',
  `send_email` int DEFAULT '0' COMMENT '是否发送邮件通知 0-不发送 1-发送',
  `category_id` int DEFAULT NULL COMMENT '分类ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article`
--

LOCK TABLES `article` WRITE;
/*!40000 ALTER TABLE `article` DISABLE KEYS */;
INSERT INTO `article` VALUES (2,'MyBatis-Plus使用指南','MyBatis-Plus是MyBatis的增强工具...',11,85,1,'2026-05-13 08:43:50','2026-05-13 08:43:50',NULL,0,1,0,NULL),(3,'Redis缓存实战','Redis是高性能的键值数据库...',4,200,1,'2026-05-13 08:43:50','2026-05-13 08:43:50',NULL,0,1,0,NULL),(4,'前端Vue3学习笔记','Vue3带来了全新的组合式API...',4,150,1,'2026-05-13 08:43:50','2026-05-13 08:43:50',NULL,0,1,0,NULL),(5,'Docker容器化部署','Docker让部署变得简单...',11,95,0,'2026-05-13 08:43:50','2026-05-13 08:43:50',NULL,0,1,0,NULL),(15,'11','11',4,0,1,'2026-05-21 10:20:18','2026-05-21 10:20:18',NULL,0,1,0,NULL),(16,'22','22',4,0,1,'2026-05-21 10:21:21','2026-05-21 10:21:21',NULL,0,1,0,NULL),(17,'33','33',4,0,1,'2026-05-21 11:00:07','2026-05-21 11:00:07',NULL,0,1,0,NULL),(18,'44','44',4,0,1,'2026-05-21 11:01:01','2026-05-21 11:01:01',NULL,0,1,0,NULL),(19,'55','55',4,0,1,'2026-05-21 11:01:48','2026-05-21 11:01:48',NULL,0,1,0,NULL),(21,'【测试】Vue 3 组合式 API 完全指南','Vue 3 引入的组合式 API 为组件开发带来了全新的编程范式...',1,0,1,'2026-05-26 20:11:07','2026-05-26 20:12:33','2026-05-26 20:12:07',0,1,0,NULL),(22,'【测试】Docker 容器化部署实战','Docker 已经成为现代应用部署的标准工具...',1,11,1,'2026-05-26 20:11:07','2026-05-26 20:12:33','2026-05-26 20:12:07',1,1,0,NULL),(23,'【测试】Spring Security 安全认证详解','Spring Security 是 Spring 生态中最流行的安全框架...',1,0,1,'2026-05-26 20:11:07','2026-05-26 20:13:33','2026-05-26 20:13:07',0,1,0,NULL),(31,'xhh','xhh',4,0,1,'2026-05-27 21:21:53','2026-05-27 21:23:56','2026-05-27 21:23:00',0,1,0,NULL),(32,'lzr','lzr',4,0,0,'2026-05-27 21:45:42','2026-05-27 21:45:42',NULL,0,1,0,NULL),(33,'Spring Boot 入门教程','Spring Boot 是 Spring 框架的扩展，简化了基于 Spring 的应用开发。本文介绍如何快速搭建第一个 Spring Boot 应用。',4,120,1,'2026-05-27 22:57:07','2026-05-27 22:57:07','2026-05-26 22:57:07',0,1,0,NULL),(34,'MyBatis-Plus 使用指南','MyBatis-Plus 是 MyBatis 的增强工具，简化开发。本文介绍 CRUD 操作、条件构造器、分页插件等核心功能。',4,89,1,'2026-05-27 22:57:07','2026-05-27 22:57:07','2026-05-25 22:57:07',0,1,0,NULL),(35,'Redis 缓存实战','Redis 是高性能的键值对数据库。本文介绍 Redis 在 Spring Boot 中的集成和使用，包括缓存注解、分布式锁等。',4,245,1,'2026-05-27 22:57:07','2026-05-27 22:57:07','2026-05-24 22:57:07',1,1,0,NULL),(36,'Vue 3 组合式 API','Vue 3 引入组合式 API，提供了更灵活的代码组织方式。本文通过实例讲解 ref、reactive、computed 等核心函数。',4,367,1,'2026-05-27 22:57:07','2026-05-27 22:57:07','2026-05-23 22:57:07',0,1,0,NULL),(37,'Docker 容器化部署','Docker 是现代应用部署的标准工具。本文介绍 Dockerfile 编写、镜像构建、容器运行等基础知识。',4,178,1,'2026-05-27 22:57:07','2026-05-27 22:57:07','2026-05-22 22:57:07',0,0,0,NULL),(38,'MySQL 索引优化','索引是提升数据库查询性能的关键。本文讲解 B+Tree 索引原理、索引设计原则以及慢查询优化技巧。',4,423,1,'2026-05-27 22:57:07','2026-05-27 22:57:07','2026-05-21 22:57:07',0,1,0,NULL),(39,'Spring Security 安全框架','Spring Security 是强大的安全框架。本文介绍认证、授权、JWT Token 等核心概念和实现方式。',4,156,1,'2026-05-27 22:57:07','2026-05-27 22:57:07','2026-05-20 22:57:07',0,1,0,NULL),(40,'前端工程化实践','前端工程化是现代前端开发的必经之路。本文讲解 Webpack 配置、代码分割、性能优化等实践技巧。',4,98,1,'2026-05-27 22:57:07','2026-05-27 22:57:07','2026-05-19 22:57:07',0,1,0,NULL),(41,'微服务架构设计','微服务架构将单体应用拆分为独立服务。本文介绍服务注册发现、配置中心、网关等核心组件。',4,234,1,'2026-05-27 22:57:07','2026-05-27 22:57:07','2026-05-18 22:57:07',0,1,0,NULL),(42,'Git 团队协作规范','Git 是分布式版本控制工具。本文介绍 Git Flow 工作流、Commit 规范、分支管理策略等团队协作实践。',4,87,1,'2026-05-27 22:57:07','2026-05-27 22:57:07','2026-05-17 22:57:07',0,1,0,NULL),(43,'立即发布','立即发布',4,4,1,'2026-05-28 00:24:50','2026-05-28 00:24:50','2026-05-28 00:24:50',1,1,0,NULL),(45,'立即发布测试-不推送邮件','立即发布测试，不推送邮件',4,0,1,'2026-05-28 00:50:38','2026-05-28 00:50:38','2026-05-28 00:50:38',0,1,0,NULL),(46,'立即发布测试-推送邮件','立即发布，推送邮件',4,1,1,'2026-05-28 00:52:27','2026-05-28 00:52:27','2026-05-28 00:52:27',0,1,1,NULL),(57,'定时发布-邮件推送','定时发布-邮件推送',4,0,1,'2026-05-28 11:07:11','2026-05-28 11:08:28','2026-05-28 11:08:00',0,1,1,NULL),(58,'异步测试','异步测试',4,1,1,'2026-05-28 11:47:31','2026-05-28 11:47:31','2026-05-28 11:47:31',0,0,0,NULL),(59,'33333','333333',4,0,1,'2026-05-28 12:00:14','2026-05-28 12:03:10','2026-05-28 12:03:00',0,1,1,NULL);
/*!40000 ALTER TABLE `article` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `article_tag`
--

DROP TABLE IF EXISTS `article_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article_tag` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `article_id` int NOT NULL COMMENT '文章ID',
  `tag_id` int NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章标签关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article_tag`
--

LOCK TABLES `article_tag` WRITE;
/*!40000 ALTER TABLE `article_tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `article_tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `slug` varchar(100) NOT NULL COMMENT 'URL别名',
  `description` varchar(255) DEFAULT NULL COMMENT '分类描述',
  `parent_id` int DEFAULT '0' COMMENT '父分类ID',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_slug` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'技术分享','tech','Java、Spring、Vue等技术文章',0,1,'2026-06-03 22:59:20','2026-06-03 22:59:20'),(2,'生活随笔','life','生活感悟、日常记录',0,2,'2026-06-03 22:59:20','2026-06-03 22:59:20'),(3,'学习笔记','study','学习过程中的笔记总结',0,3,'2026-06-03 22:59:20','2026-06-03 22:59:20'),(4,'项目实战','project','项目开发实战经验',0,4,'2026-06-03 22:59:20','2026-06-03 22:59:20');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '评论ID，主键',
  `article_id` int NOT NULL COMMENT '文章ID，关联article表',
  `user_id` int NOT NULL COMMENT '评论用户ID，关联users表',
  `content` text NOT NULL COMMENT '评论内容',
  `parent_id` int DEFAULT '0' COMMENT '父评论ID，0表示顶级评论',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-待审核，1-已发布，2-已删除',
  `like_count` int DEFAULT '0' COMMENT '点赞数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
INSERT INTO `comment` VALUES (1,22,4,'这是一条测试评论',0,1,0,'2026-05-27 23:25:09','2026-05-27 23:25:09'),(2,22,4,'这是一条测试评论2',0,1,0,'2026-05-27 23:49:01','2026-05-27 23:49:01'),(3,22,4,'这是一条测试评论3',0,1,0,'2026-05-27 23:53:30','2026-05-27 23:53:30'),(4,22,4,'这是一条测试评论4',0,1,0,'2026-05-27 23:54:48','2026-05-27 23:54:48'),(5,22,4,'这是一条测试评论5',0,1,0,'2026-05-27 23:55:18','2026-05-27 23:55:18');
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `persistent_logins`
--

DROP TABLE IF EXISTS `persistent_logins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `persistent_logins` (
  `username` varchar(64) NOT NULL,
  `series` varchar(64) NOT NULL,
  `token` varchar(64) NOT NULL,
  `last_used` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`series`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `persistent_logins`
--

LOCK TABLES `persistent_logins` WRITE;
/*!40000 ALTER TABLE `persistent_logins` DISABLE KEYS */;
INSERT INTO `persistent_logins` VALUES ('user004','4LA1F3soE3XhqEvxzQLWkg==','3kKC24J8Z8BCSQE9x4/P0A==','2026-06-23 08:53:12');
/*!40000 ALTER TABLE `persistent_logins` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `school`
--

DROP TABLE IF EXISTS `school`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `school` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学校名称',
  `city` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所在城市',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `school`
--

LOCK TABLES `school` WRITE;
/*!40000 ALTER TABLE `school` DISABLE KEYS */;
INSERT INTO `school` VALUES (1,'武汉大学','武汉','2026-06-23 16:44:30','2026-06-23 16:44:30'),(2,'华中科技大学','武汉','2026-06-23 16:44:30','2026-06-23 16:44:30'),(3,'武汉理工大学','武汉','2026-06-23 16:44:30','2026-06-23 16:44:30'),(4,'华中师范大学','武汉','2026-06-23 16:44:30','2026-06-23 16:44:30'),(5,'中国地质大学（武汉）','武汉','2026-06-23 16:44:30','2026-06-23 16:44:30'),(6,'中南财经政法大学','武汉','2026-06-23 16:44:30','2026-06-23 16:44:30'),(7,'湖北大学','武汉','2026-06-23 16:44:30','2026-06-23 16:44:30'),(8,'武汉科技大学','武汉','2026-06-23 16:44:30','2026-06-23 16:44:30');
/*!40000 ALTER TABLE `school` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tag` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(50) NOT NULL COMMENT '标签名称',
  `slug` varchar(50) NOT NULL COMMENT 'URL别名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tag`
--

LOCK TABLES `tag` WRITE;
/*!40000 ALTER TABLE `tag` DISABLE KEYS */;
/*!40000 ALTER TABLE `tag` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `school_id` int DEFAULT NULL COMMENT '学校ID',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `role` int DEFAULT '0' COMMENT '角色：0普通用户，1管理员',
  `status` int DEFAULT '1' COMMENT '状态：0禁用，1正常',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'user001','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','张三','zhangsan@campus.com','13800000001',1,'/avatars/user001.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(2,'user002','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','李四','lisi@campus.com','13800000002',1,'/avatars/user002.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(3,'user003','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','王五','wangwu@campus.com','13800000003',2,'/avatars/user003.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(4,'user004','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','赵六','zhaoliu@campus.com','13800000004',2,'/avatars/474905515d02431da9b4a18e085d921f.png',1,1,'2026-05-12 09:25:04','2026-05-12 09:25:53'),(5,'user005','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','小明','xiaoming@campus.com','13800000005',3,'/avatars/user005.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(6,'user006','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','小红','xiaohong@campus.com','13800000006',3,'/avatars/user006.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(7,'user007','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','小刚','xiaogang@campus.com','13800000007',1,'/avatars/user007.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(8,'user008','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','小丽','xiaoli@campus.com','13800000008',1,'/avatars/user008.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(9,'user009','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','陈晨','chenchen@campus.com','13800000009',2,'/avatars/user009.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(10,'user010','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','刘洋','liuyang@campus.com','13800000010',2,'/avatars/user010.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(11,'user011','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','周杰','zhoujie@campus.com','13800000011',3,'/avatars/user011.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(12,'user012','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','吴迪','wudi@campus.com','13800000012',3,'/avatars/user012.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(13,'user013','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','郑爽','zhengshuang@campus.com','13800000013',1,'/avatars/user013.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(14,'user014','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','林娜','linna@campus.com','13800000014',1,'/avatars/user014.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(15,'user015','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','郭峰','guofeng@campus.com','13800000015',2,'/avatars/user015.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(16,'user016','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','唐雅','tangya@campus.com','13800000016',2,'/avatars/user016.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(17,'user017','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','孙阳','sunyang@campus.com','13800000017',3,'/avatars/user017.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(18,'user018','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','宋茜','songqian@campus.com','13800000018',3,'/avatars/user018.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(19,'user019','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','杨颖','yangying@campus.com','13800000019',1,'/avatars/user019.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(20,'user020','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','黄轩','huangxuan@campus.com','13800000020',1,'/avatars/user020.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(21,'user021','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','张艺兴','zhangyixing@campus.com','13800000021',2,'/avatars/user021.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(22,'user022','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','迪丽热巴','dilireba@campus.com','13800000022',2,'/avatars/user022.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(23,'user023','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','古力娜扎','gulinazha@campus.com','13800000023',3,'/avatars/user023.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(24,'user024','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','刘亦菲','liuyifei@campus.com','13800000024',3,'/avatars/user024.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(25,'user025','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','彭于晏','penguyan@campus.com','13800000025',1,'/avatars/user025.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(26,'user026','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','胡歌','huge@campus.com','13800000026',1,'/avatars/user026.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(27,'user027','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','霍建华','huojianhua@campus.com','13800000027',2,'/avatars/user027.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(28,'user028','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','靳东','jindong@campus.com','13800000028',2,'/avatars/user028.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(29,'user029','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','王凯','wangkai@campus.com','13800000029',3,'/avatars/user029.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(30,'user030','$2a$12$bOdJaXF25m3426qUrXk8SueqLtcLSl2oOn36ocfiy99ScklwoYNb6','杨幂','yangmi@campus.com','13800000030',3,'/avatars/user030.jpg',0,1,'2026-05-12 09:25:04','2026-05-12 09:25:04'),(31,'user056','156565','','','',NULL,'/avatars/4088cf30cc804259a0f795ef9b5126e1.png',0,1,'2026-05-26 19:41:15','2026-05-26 19:41:15'),(35,'user0111','156565','','','',NULL,'/avatars/cce030105ca248b2adc732f82eb2e6ce.png',0,0,'2026-05-26 19:55:32','2026-06-23 16:42:47'),(37,'ksbird','$2a$10$ZDFLBTv83UQym8.4tOMY9eDmUFUfB4eH5baVg8Jv1oJisKZb19KZK','小鸟','1538292542@qq.com','12345678989',1,NULL,0,1,'2026-06-23 16:54:10','2026-06-23 16:54:10');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

       --
-- Table structure for table `favorite`
--

DROP TABLE IF EXISTS `favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite` (
                            `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `user_id` int NOT NULL COMMENT '用户ID',
                            `article_id` int NOT NULL COMMENT '商品ID',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_user_article` (`user_id`,`article_id`),
                            KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorite`
--

LOCK TABLES `favorite` WRITE;
/*!40000 ALTER TABLE `favorite` DISABLE KEYS */;
/*!40000 ALTER TABLE `favorite` ENABLE KEYS */;
UNLOCK TABLES;
-- Dump completed on 2026-06-23 17:18:55