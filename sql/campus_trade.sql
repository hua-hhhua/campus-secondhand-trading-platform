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
  `wechat` varchar(50) DEFAULT NULL COMMENT '微信号',
  `qq` varchar(20) DEFAULT NULL COMMENT 'QQ号',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '价格',
  `stock` int DEFAULT '1' COMMENT '库存',
  `school_id` int DEFAULT NULL COMMENT '学校ID',
  `location` varchar(100) DEFAULT NULL COMMENT '交易地点',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图',
  `product_status` int DEFAULT '0' COMMENT '商品状态：0在售 1已售 2下架',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `article`
--

LOCK TABLES `article` WRITE;
/*!40000 ALTER TABLE `article` DISABLE KEYS */;
INSERT INTO `article` VALUES (1,'iPhone 14 Pro 256G 深空黑','九成新，使用半年，无划痕，电池健康度92%',37,1,1,'2026-06-23 22:14:22','2026-06-23 22:15:49','2026-06-23 22:15:49',1,1,0,6,'','','13800000001',5299.00,1,1,'信息学部9舍','/uploads/articles/32fbd85b-f063-4f89-8e20-1d2756f7abaf.png',0),(2,'华为MateBook 14 2023款','i5处理器，16G内存，512G固态，2K触控屏',37,0,1,'2026-06-23 22:14:22','2026-06-23 22:16:07','2026-06-23 22:16:07',0,1,0,7,'','','13800000002',3999.00,1,1,'文理学部5舍','/uploads/articles/3711ceb0-a0ba-4b82-9153-ea9a183aaf18.png',0),(3,'考研数学全套资料','张宇基础30讲+强化36讲+真题大全解+模拟卷',37,0,1,'2026-06-23 22:14:22','2026-06-23 22:16:21','2026-06-23 22:16:21',0,1,0,12,'','','13800000003',120.00,2,2,'紫菘公寓','/uploads/articles/0d497028-1802-4225-8a4f-aff7a6d2cee0.png',0),(4,'雅迪电动车','续航60公里，带原装充电器，毕业出售',37,0,1,'2026-06-23 22:14:22','2026-06-23 22:16:38','2026-06-23 22:16:38',0,1,0,9,'','','13800000004',800.00,1,2,'韵苑公寓','/uploads/articles/63ad02ec-85d1-45fd-aca5-1200a4daef52.png',0),(5,'二手iPad Air 5','64G蓝色，带原装笔，屏幕无划痕',37,11,1,'2026-06-23 22:14:22','2026-06-23 22:17:30','2026-06-23 22:17:30',1,1,0,6,'','','13800000005',2800.00,1,4,'国交','/uploads/articles/e5afb180-260b-4c18-90e8-053ce2789952.png',0),(6,'电煮锅','1.2L宿舍用，带蒸笼，可煮面煮粥',4,2,1,'2026-06-23 22:14:22','2026-06-23 22:14:22','2026-06-23 22:14:22',0,1,0,9,NULL,NULL,'13800000006',45.00,3,6,'北苑',NULL,0),(7,'Nike空军一号','白色经典款42码，几乎全新，原盒',4,4,1,'2026-06-23 22:14:22','2026-06-23 22:14:22','2026-06-23 22:14:22',0,1,0,10,NULL,NULL,'13800000007',450.00,1,5,'北区',NULL,0),(8,'家用投影仪','小米投影仪青春版，1080P，支持手机投屏',4,7,1,'2026-06-23 22:14:22','2026-06-23 22:14:22','2026-06-23 22:14:22',0,1,0,9,NULL,NULL,'13800000008',550.00,1,5,'西区',NULL,0),(9,'小米手环8 Pro','全新未拆封，年会奖品，黑色',1,1,1,'2026-06-23 22:14:22','2026-06-23 22:14:22','2026-06-23 22:14:22',0,1,0,6,NULL,NULL,'13800000009',280.00,2,1,'梅园',NULL,0),(10,'全新AirPods Pro 2','全新未拆封，朋友送的',1,2,1,'2026-06-23 22:14:22','2026-06-23 22:14:22','2026-06-23 22:14:22',0,1,0,6,NULL,NULL,'13800000010',1350.00,1,3,'鉴湖校区',NULL,0),(11,'Switch游戏卡带','塞尔达传说：王国之泪，日版带中文',1,3,1,'2026-06-23 22:14:22','2026-06-23 22:14:22','2026-06-23 22:14:22',0,1,0,6,NULL,NULL,'13800000011',280.00,1,2,'沁苑',NULL,0),(12,'毕业季桌子','60*100cm宿舍书桌，带书架',1,0,1,'2026-06-23 22:14:22','2026-06-23 22:14:22','2026-06-23 22:14:22',0,1,0,9,NULL,NULL,'13800000012',120.00,2,2,'东校区',NULL,0),(13,'机械革命游戏本','i7-12700H+RTX3060+16G+512G',2,19,1,'2026-06-23 22:14:22','2026-06-23 22:14:22','2026-06-23 22:14:22',1,1,0,7,NULL,NULL,'13800000013',4500.00,1,3,'南湖校区',NULL,0),(14,'羽毛球拍','尤尼克斯天斧99，4U5，拉24磅',2,6,1,'2026-06-23 22:14:22','2026-06-23 22:14:22','2026-06-23 22:14:22',0,1,0,11,NULL,NULL,'13800000014',380.00,2,6,'南苑',NULL,0),(15,'小米无线充电宝','10000mAh，支持无线充电',2,4,1,'2026-06-23 22:14:22','2026-06-23 22:14:22','2026-06-23 22:14:22',0,1,0,6,NULL,NULL,'13800000015',120.00,3,8,'黄家湖',NULL,0),(16,'骆驼户外帐篷','双人自动速开帐篷，防雨防晒',2,3,1,'2026-06-23 22:14:22','2026-06-23 22:14:22','2026-06-23 22:14:22',0,1,0,11,NULL,NULL,'13800000016',180.00,2,7,'一期公寓',NULL,0),(17,'二手iPhone 12','白色128G，换过电池，健康度88%',3,9,1,'2026-06-23 22:14:22','2026-06-23 22:14:22','2026-06-23 22:14:22',0,1,0,6,NULL,NULL,'13800000017',1800.00,1,3,'鉴湖校区',NULL,0),(18,'英语四六级全套','四级真题+六级真题+词汇书',3,2,1,'2026-06-23 22:14:22','2026-06-23 22:14:22','2026-06-23 22:14:22',0,1,0,8,NULL,NULL,'13800000018',50.00,3,4,'东区',NULL,0),(19,'考研英语黄皮书','2005-2024年真题全套，详细解析',3,6,1,'2026-06-23 22:14:22','2026-06-23 22:14:22','2026-06-23 22:14:22',0,1,0,12,NULL,NULL,'13800000019',80.00,2,8,'青山校区',NULL,0),(20,'电竞椅','黑色人体工学，可躺可转，厚坐垫',3,7,1,'2026-06-23 22:14:22','2026-06-23 22:14:22','2026-06-23 22:14:22',0,1,0,9,NULL,NULL,'13800000020',350.00,1,1,'枫园',NULL,0),(21,'键盘','九成新白色科技感键盘，有意者加联系方式详谈',37,0,1,'2026-06-24 01:00:24','2026-06-24 01:00:24','2026-06-24 01:00:24',0,1,0,6,'','','13800000004',200.00,1,3,'紫菘公寓','/uploads/articles/0b75c513-426a-4ab9-a243-26b7ab12141b.png',0);
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'技术分享','tech','Java、Spring、Vue等技术文章',0,1,'2026-06-03 22:59:20','2026-06-03 22:59:20'),(2,'生活随笔','life','生活感悟、日常记录',0,2,'2026-06-03 22:59:20','2026-06-03 22:59:20'),(3,'学习笔记','study','学习过程中的笔记总结',0,3,'2026-06-03 22:59:20','2026-06-03 22:59:20'),(4,'项目实战','project','项目开发实战经验',0,4,'2026-06-03 22:59:20','2026-06-03 22:59:20'),(6,'手机数码','phone','手机、平板、耳机等数码产品',0,1,'2026-06-23 20:23:57','2026-06-23 20:23:57'),(7,'电脑配件','computer','笔记本、台式机、配件等',0,2,'2026-06-23 20:23:57','2026-06-23 20:23:57'),(8,'图书教材','book','教材、小说、杂志等',0,3,'2026-06-23 20:23:57','2026-06-23 20:23:57'),(9,'生活用品','daily','宿舍用品、收纳、电器等',0,4,'2026-06-23 20:23:57','2026-06-23 20:23:57'),(10,'服饰鞋包','clothing','衣服、鞋子、包包等',0,5,'2026-06-23 20:23:57','2026-06-23 20:23:57'),(11,'运动户外','sports','球类、健身器材、户外装备等',0,6,'2026-06-23 20:23:57','2026-06-23 20:23:57'),(12,'考研资料','kaoyan','考研书籍、真题、笔记等',0,7,'2026-06-23 20:23:57','2026-06-23 20:23:57'),(13,'毕业季清仓','graduation','毕业季甩卖专区',0,8,'2026-06-23 20:23:57','2026-06-23 20:23:57');
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorite`
--

LOCK TABLES `favorite` WRITE;
/*!40000 ALTER TABLE `favorite` DISABLE KEYS */;
INSERT INTO `favorite` VALUES (2,37,13,'2026-06-23 23:31:38');
/*!40000 ALTER TABLE `favorite` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章标签表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tag`
--

LOCK TABLES `tag` WRITE;
/*!40000 ALTER TABLE `tag` DISABLE KEYS */;
INSERT INTO `tag` VALUES (1,'九成新','90-new','2026-06-23 20:24:51','2026-06-23 20:24:51'),(2,'八成新','80-new','2026-06-23 20:24:51','2026-06-23 20:24:51'),(3,'全新','brand-new','2026-06-23 20:24:51','2026-06-23 20:24:51'),(4,'包邮','free-shipping','2026-06-23 20:24:51','2026-06-23 20:24:51'),(5,'可议价','negotiable','2026-06-23 20:24:51','2026-06-23 20:24:51'),(6,'急售','urgent','2026-06-23 20:24:51','2026-06-23 20:24:51'),(7,'当面交易','face-to-face','2026-06-23 20:24:51','2026-06-23 20:24:51'),(8,'带发票','with-invoice','2026-06-23 20:24:51','2026-06-23 20:24:51'),(9,'带保修','with-warranty','2026-06-23 20:24:51','2026-06-23 20:24:51'),(10,'毕业季清仓','graduation-sale','2026-06-23 20:24:51','2026-06-23 20:24:51'),(11,'全套','full-set','2026-06-23 20:24:51','2026-06-23 20:24:51'),(12,'可拆卖','can-split','2026-06-23 20:24:51','2026-06-23 20:24:51');
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

-- Dump completed on 2026-06-24 21:14:09
