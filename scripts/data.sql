CREATE DATABASE  IF NOT EXISTS `roamblue_cloud_management` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */;
USE `roamblue_cloud_management`;
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
-- Dumping data for table `tbl_calculation_scheme`
--

LOCK TABLES `tbl_calculation_scheme` WRITE;
/*!40000 ALTER TABLE `tbl_calculation_scheme` DISABLE KEYS */;
INSERT INTO `tbl_calculation_scheme` VALUES (7,'1核500MB',1,512000,0,'2021-05-18 03:23:26'),(8,'1核1GB',1,1048576,0,'2021-05-18 03:23:26'),(9,'1核2GB',1,2097152,0,'2021-05-18 03:23:26'),(10,'1核4GB',1,4194304,0,'2021-05-18 03:23:26'),(11,'1核8GB',1,8388608,0,'2021-05-18 03:23:26'),(12,'2核1GB',2,1048576,0,'2021-05-18 03:23:26'),(13,'2核2GB',2,2097152,0,'2021-05-18 03:23:26'),(14,'2核4GB',2,4194304,0,'2021-05-18 03:23:26'),(15,'2核8GB',2,8388608,0,'2021-05-18 03:23:26'),(16,'2核16GB',2,16777216,0,'2021-05-18 03:23:26'),(17,'4核4G',4,4194304,0,'2021-05-18 03:23:26'),(18,'4核8G',4,8388608,0,'2021-05-18 03:23:26'),(19,'4核16G',4,16777216,0,'2021-05-18 03:23:26'),(21,'8核16G',8,16777216,0,'2021-05-18 03:23:26');
/*!40000 ALTER TABLE `tbl_calculation_scheme` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `tbl_login_info`
--

LOCK TABLES `tbl_login_info` WRITE;
/*!40000 ALTER TABLE `tbl_login_info` DISABLE KEYS */;
INSERT INTO `tbl_login_info` VALUES (1,'admin','5a1a678a1a32aca736f51a0645eaf1393b301b5508f539683d096004f163601e','CRY:aFLMrGruvqTXrIg1',0,1,'2021-06-01 07:00:33');
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
-- Dumping data for table `tbl_permission_category`
--

LOCK TABLES `tbl_permission_category` WRITE;
/*!40000 ALTER TABLE `tbl_permission_category` DISABLE KEYS */;
INSERT INTO `tbl_permission_category` VALUES (1,'集群管理',1),(2,'网络管理',2),(3,'主机管理',3),(4,'存储管理',4),(5,'模版管理',5),(6,'磁盘管理',6),(7,'计算方案管理',7),(8,'系统分类管理',8),(9,'权限管理',9),(10,'VM管理',10),(11,'群组管理',11),(12,'用户管理',12);
/*!40000 ALTER TABLE `tbl_permission_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `tbl_permission_info`
--

LOCK TABLES `tbl_permission_info` WRITE;
/*!40000 ALTER TABLE `tbl_permission_info` DISABLE KEYS */;
INSERT INTO `tbl_permission_info` VALUES (1,'8','category.create','系统分类创建',101),(2,'8','category.destroy','系统分类销毁',103),(3,'8','category.modify','系统分类修改',102),(4,'1','cluster.create','集群创建',201),(5,'1','cluster.destroy','集群销毁',203),(6,'1','cluster.modify','集群修改',202),(7,'11','group.create','群组创建',301),(8,'11','group.destroy','群组销毁',303),(9,'11','group.modify','群组修改',302),(10,'3','host.create','主机创建',401),(11,'3','host.destroy','主机销毁',403),(12,'3','host.status.modify','主机修改',402),(13,'2','network.create','网络创建',501),(14,'2','network.destroy','网络销毁',503),(15,'2','network.status.modify','网络状态变更',502),(16,'7','scheme.create','计算方案创建',601),(17,'7','scheme.destroy','计算方案销毁',602),(18,'4','storage.create','存储池创建',701),(19,'4','storage.destroy','存储池销毁',702),(20,'5','template.create','模版创建',801),(21,'5','template.destroy','模版销毁',802),(22,'12','user.destroy','用户销毁',1205),(23,'12','user.password.reset','用户重置密码',1204),(24,'12','user.permission.update','用户权限修改',1203),(25,'12','user.register','用户注册',1201),(26,'12','user.state.update','用户状态变更',1203),(27,'10','vm.cd.update','VM挂载\\卸载光盘',903),(28,'10','vm.create','VM创建',901),(29,'10','vm.destroy','VM销毁',910),(30,'10','vm.disk.update','VM挂载\\卸载磁盘',904),(31,'10','vm.modify','VM信息修改',902),(32,'10','vm.nic.update','VM添加\\销毁网卡',905),(33,'10','vm.reinstall','VM重装',906),(34,'10','vm.resume','VM恢复',909),(35,'10','vm.status.update','VM启动、停止操作',907),(36,'10','vm.template','VM模版创建',908),(37,'6','volume.create','磁盘创建',1001),(38,'6','volume.destroy','磁盘销毁',1003),(39,'6','volume.resize','磁盘扩容',1002),(40,'6','volume.resume','磁盘恢复',1004),(41,'9','rule.permission.create','权限组创建',1101),(42,'9','rule.permission.modify','权限组修改',1102),(43,'9','rule.permission.destroy','权限组销毁',1103);
/*!40000 ALTER TABLE `tbl_permission_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `tbl_rule_permission`
--

LOCK TABLES `tbl_rule_permission` WRITE;
/*!40000 ALTER TABLE `tbl_rule_permission` DISABLE KEYS */;
INSERT INTO `tbl_rule_permission` VALUES (1,'超级管理员','4,6,5,13,15,14,10,12,11,18,19,20,21,37,39,38,40,16,17,1,3,2,41,42,43,28,31,27,30,32,33,35,36,34,29,7,9,8,25,26,24,23,22'),(2,'管理员','4,6,5,13,15,14,10,12,11,18,19,20,21,37,39,38,40,16,17,1,3,2,41,42,43,28,31,27,30,32,33,35,36,34,29,7,9,8,25,26,24,23,22');
/*!40000 ALTER TABLE `tbl_rule_permission` ENABLE KEYS */;
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

-- Dump completed on 2021-08-20 11:06:13
