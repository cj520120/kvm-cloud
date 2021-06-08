-- MySQL dump 10.13  Distrib 5.7.17, for macos10.12 (x86_64)
--
-- Host: 192.168.1.92    Database: roamblue_cloud_management
-- ------------------------------------------------------
-- Server version	5.7.22

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `tbl_login_info`
--

LOCK TABLES `tbl_login_info` WRITE;
/*!40000 ALTER TABLE `tbl_login_info` DISABLE KEYS */;
INSERT INTO `tbl_login_info` VALUES (1,'admin','5a1a678a1a32aca736f51a0645eaf1393b301b5508f539683d096004f163601e','CRY:aFLMrGruvqTXrIg1',0,0,'2021-06-01 07:00:33');
/*!40000 ALTER TABLE `tbl_login_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `tbl_os_category`
--

LOCK TABLES `tbl_os_category` WRITE;
/*!40000 ALTER TABLE `tbl_os_category` DISABLE KEYS */;
INSERT INTO `tbl_os_category` VALUES (1,'Centos','virtio','virtio','2021-05-18 02:37:24'),(2,'Ubuntu','virtio','virtio','2021-05-18 02:37:24'),(3,'Windows','rtl8139','ide','2021-05-18 02:37:24');
/*!40000 ALTER TABLE `tbl_os_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'roamblue_cloud_management'
--

--
-- Dumping routines for database 'roamblue_cloud_management'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-06-08 12:15:06
