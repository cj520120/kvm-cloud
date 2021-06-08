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
-- Table structure for table `tbl_calculation_scheme`
--

DROP TABLE IF EXISTS `tbl_calculation_scheme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_calculation_scheme` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `scheme_name` varchar(45) NOT NULL,
  `scheme_cpu` int(11) NOT NULL,
  `scheme_memory` bigint(20) NOT NULL,
  `scheme_cpu_speed` int(11) NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_cluster_info`
--

DROP TABLE IF EXISTS `tbl_cluster_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_cluster_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cluster_name` varchar(45) NOT NULL,
  `cluster_status` varchar(45) NOT NULL DEFAULT 'Ready',
  `over_cpu` float NOT NULL DEFAULT '1',
  `over_memory` float NOT NULL DEFAULT '1',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_group_info`
--

DROP TABLE IF EXISTS `tbl_group_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_group_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_name` varchar(45) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_host_info`
--

DROP TABLE IF EXISTS `tbl_host_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_host_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cluster_id` int(11) NOT NULL,
  `host_name` varchar(45) NOT NULL,
  `host_ip` varchar(45) NOT NULL,
  `host_uri` varchar(255) NOT NULL,
  `host_cpu` int(11) NOT NULL,
  `host_memory` bigint(20) NOT NULL,
  `host_allocation_cpu` int(11) NOT NULL,
  `host_allocation_memory` bigint(20) NOT NULL,
  `host_status` varchar(45) NOT NULL DEFAULT 'Ready',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_lock_info`
--

DROP TABLE IF EXISTS `tbl_lock_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_lock_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lock_name` varchar(45) NOT NULL,
  `lock_uuid` varchar(45) NOT NULL,
  `lock_thread` bigint(20) NOT NULL,
  `lock_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `lock_timeout` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `IX_KEY` (`lock_name`)
) ENGINE=InnoDB AUTO_INCREMENT=2883055 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_login_info`
--

DROP TABLE IF EXISTS `tbl_login_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_login_info` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `login_name` varchar(10) NOT NULL,
  `login_password` varchar(64) NOT NULL COMMENT 'SHA_256(PWD+”:”+SALT)',
  `login_password_salt` varchar(45) NOT NULL COMMENT 'SHA2(concat("111111",":",login_password_salt),256)',
  `login_state` smallint(3) NOT NULL DEFAULT '0',
  `rule_type` int(11) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `IX_LOGIN_NAME` (`login_name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_management_info`
--

DROP TABLE IF EXISTS `tbl_management_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_management_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `server_id` varchar(32) NOT NULL,
  `last_active_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `IX_SERVER_ID` (`server_id`)
) ENGINE=InnoDB AUTO_INCREMENT=695 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_network_info`
--

DROP TABLE IF EXISTS `tbl_network_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_network_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cluster_id` int(11) NOT NULL,
  `network_name` varchar(45) NOT NULL,
  `network_subnet` varchar(45) NOT NULL,
  `network_gateway` varchar(45) NOT NULL,
  `network_dns` varchar(255) NOT NULL,
  `network_manager_start_ip` varchar(45) NOT NULL DEFAULT '',
  `network_manager_end_ip` varchar(45) NOT NULL DEFAULT '',
  `network_guest_start_ip` varchar(45) NOT NULL,
  `network_guest_end_ip` varchar(45) NOT NULL,
  `network_card` varchar(45) NOT NULL,
  `network_type` varchar(45) NOT NULL DEFAULT 'Bridge',
  `network_status` varchar(45) NOT NULL DEFAULT 'Ready',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_os_category`
--

DROP TABLE IF EXISTS `tbl_os_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_os_category` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `category_name` varchar(45) NOT NULL,
  `network_driver` varchar(45) NOT NULL,
  `disk_driver` varchar(45) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_storage_info`
--

DROP TABLE IF EXISTS `tbl_storage_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_storage_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cluster_id` int(11) NOT NULL,
  `storage_name` varchar(45) NOT NULL,
  `storage_host` varchar(45) NOT NULL,
  `storage_source` varchar(45) NOT NULL,
  `storage_target` varchar(45) NOT NULL,
  `storage_capacity` bigint(20) NOT NULL,
  `storage_available` bigint(20) NOT NULL,
  `storage_allocation` bigint(20) NOT NULL,
  `storage_status` varchar(45) NOT NULL DEFAULT 'Ready',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_sys_vm_info`
--

DROP TABLE IF EXISTS `tbl_sys_vm_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_sys_vm_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `network_id` int(11) NOT NULL,
  `vm_type` varchar(45) NOT NULL,
  `vm_id` int(11) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `IX_SYS_VM` (`network_id`,`vm_type`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_task_info`
--

DROP TABLE IF EXISTS `tbl_task_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_task_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `task_name` varchar(45) NOT NULL,
  `server_id` varchar(45) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `IX_TASK_NAME` (`task_name`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_template_info`
--

DROP TABLE IF EXISTS `tbl_template_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_template_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cluster_id` int(11) NOT NULL,
  `template_name` varchar(255) NOT NULL,
  `template_type` varchar(45) NOT NULL,
  `template_uri` varchar(255) NOT NULL,
  `template_size` bigint(20) NOT NULL,
  `template_status` varchar(45) NOT NULL DEFAULT 'Ready',
  `os_category_id` int(11) NOT NULL DEFAULT '1',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_template_ref_info`
--

DROP TABLE IF EXISTS `tbl_template_ref_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_template_ref_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cluster_id` int(11) NOT NULL,
  `storage_id` int(11) NOT NULL,
  `template_id` int(11) NOT NULL,
  `template_target` varchar(45) NOT NULL,
  `template_status` varchar(255) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_vm_info`
--

DROP TABLE IF EXISTS `tbl_vm_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_vm_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cluster_id` int(11) NOT NULL,
  `vm_name` varchar(45) NOT NULL,
  `host_id` int(11) NOT NULL,
  `calculation_scheme_id` int(11) NOT NULL,
  `vm_description` varchar(45) NOT NULL,
  `vm_iso` int(11) NOT NULL,
  `vm_ip` varchar(45) NOT NULL DEFAULT '' COMMENT '主IP',
  `vnc_port` int(11) NOT NULL,
  `vnc_password` varchar(45) NOT NULL DEFAULT '',
  `vm_type` varchar(45) NOT NULL,
  `vm_status` varchar(45) NOT NULL,
  `template_id` int(11) NOT NULL DEFAULT '0',
  `os_category_id` int(11) NOT NULL DEFAULT '1',
  `group_id` int(11) NOT NULL DEFAULT '0',
  `last_update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `remove_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=255 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_vm_network`
--

DROP TABLE IF EXISTS `tbl_vm_network`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_vm_network` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `network_id` int(11) NOT NULL,
  `cluster_id` int(11) NOT NULL,
  `vm_id` int(11) NOT NULL,
  `vm_device` int(11) NOT NULL DEFAULT '0',
  `network_mac` varchar(45) NOT NULL,
  `network_ip` varchar(45) NOT NULL,
  `ip_type` varchar(45) NOT NULL DEFAULT 'Guest',
  `network_status` varchar(45) NOT NULL DEFAULT 'Ready',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=537 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_vm_statics`
--

DROP TABLE IF EXISTS `tbl_vm_statics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_vm_statics` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `vm_id` int(11) NOT NULL,
  `disk_write_speed` bigint(20) NOT NULL,
  `disk_read_speed` bigint(20) NOT NULL,
  `network_send_speed` bigint(20) NOT NULL,
  `network_receive_speed` bigint(20) NOT NULL,
  `cpu_usage` bigint(20) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `IX_INSTANCE_CREATE_TIME` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1257673 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_vnc_info`
--

DROP TABLE IF EXISTS `tbl_vnc_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_vnc_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `vnc_host` varchar(45) NOT NULL,
  `vnc_port` int(11) NOT NULL,
  `vnc_password` varchar(45) NOT NULL DEFAULT '',
  `vm_id` int(11) NOT NULL,
  `network_id` int(11) NOT NULL,
  `cluster_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `IDX_INSTANCE_ID` (`vm_id`,`network_id`,`cluster_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2118 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_volume_info`
--

DROP TABLE IF EXISTS `tbl_volume_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_volume_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cluster_id` int(11) NOT NULL,
  `storage_id` int(11) NOT NULL,
  `vm_id` int(11) NOT NULL,
  `vm_device` int(11) NOT NULL,
  `volume_target` varchar(45) NOT NULL,
  `volume_name` varchar(45) NOT NULL,
  `volume_capacity` bigint(20) NOT NULL,
  `volume_allocation` bigint(20) NOT NULL,
  `volume_status` varchar(45) NOT NULL DEFAULT 'Ready',
  `remove_time` timestamp NULL DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=307 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

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

-- Dump completed on 2021-06-08 12:13:40
