/*
SQLyog Ultimate v12.08 (64 bit)
MySQL - 5.7.22 : Database - cj_kvm_cloud
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`cj_kvm_cloud` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

USE `cj_kvm_cloud`;

-- MySQL dump 10.13  Distrib 8.0.32, for Linux (x86_64)
--
-- Host: localhost    Database: cj_kvm_cloud
-- ------------------------------------------------------
-- Server version	8.0.32

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
-- Table structure for table `tbl_component_info`
--

DROP TABLE IF EXISTS `tbl_component_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_component_info` (
  `component_id` int NOT NULL AUTO_INCREMENT,
  `component_type` int NOT NULL,
  `component_vip` varchar(45) NOT NULL,
  `basic_component_vip` varchar(45) NOT NULL,
  `network_id` int NOT NULL,
  `master_guest_id` int NOT NULL,
  `component_slave_number` int NOT NULL DEFAULT '1',
  `slave_guest_ids` varchar(128) NOT NULL DEFAULT '[]',
  PRIMARY KEY (`component_id`),
  KEY `IX_NETWORK_COMPONENT` (`component_type`,`network_id`)
) ENGINE=InnoDB AUTO_INCREMENT=224 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_dns_info`
--

DROP TABLE IF EXISTS `tbl_dns_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_dns_info` (
  `dns_id` int NOT NULL AUTO_INCREMENT,
  `network_id` int NOT NULL,
  `dns_domain` varchar(128) NOT NULL,
  `dns_ip` varchar(45) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`dns_id`),
  UNIQUE KEY `IX_NETWORK_DOMAIN` (`network_id`,`dns_domain`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_group_info`
--

DROP TABLE IF EXISTS `tbl_group_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_group_info` (
  `group_id` int NOT NULL AUTO_INCREMENT,
  `group_name` varchar(64) NOT NULL,
  `create_time` timestamp NOT NULL,
  PRIMARY KEY (`group_id`)
) ENGINE=InnoDB AUTO_INCREMENT=658 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_guest_disk`
--

DROP TABLE IF EXISTS `tbl_guest_disk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_guest_disk` (
  `guest_disk_id` int NOT NULL AUTO_INCREMENT,
  `guest_id` int NOT NULL,
  `volume_id` int NOT NULL,
  `device_id` int NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`guest_disk_id`),
  UNIQUE KEY `IX_GUEST_VOLUME` (`guest_id`,`device_id`),
  UNIQUE KEY `IX_VOLUME_ID` (`volume_id`)
) ENGINE=InnoDB AUTO_INCREMENT=587 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_guest_info`
--

DROP TABLE IF EXISTS `tbl_guest_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_guest_info` (
  `guest_id` int NOT NULL AUTO_INCREMENT,
  `guest_name` varchar(45) NOT NULL,
  `guest_description` varchar(45) NOT NULL,
  `guest_bus_type` varchar(10) NOT NULL,
  `guest_cpu` int NOT NULL,
  `guest_cpu_speed` int NOT NULL DEFAULT '0',
  `guest_memory` bigint NOT NULL,
  `guest_cd_room` int NOT NULL,
  `host_id` int NOT NULL,
  `last_host_id` int NOT NULL,
  `scheme_id` int NOT NULL DEFAULT '0',
  `guest_ip` varchar(48) NOT NULL DEFAULT '',
  `network_id` varchar(45) NOT NULL DEFAULT '0',
  `guest_type` int NOT NULL,
  `group_id` int NOT NULL DEFAULT '0',
  `other_id` int NOT NULL,
  `guest_status` int NOT NULL,
  `last_start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`guest_id`)
) ENGINE=InnoDB AUTO_INCREMENT=524 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_guest_network`
--

DROP TABLE IF EXISTS `tbl_guest_network`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_guest_network` (
  `guest_network_id` int NOT NULL AUTO_INCREMENT,
  `network_id` int NOT NULL,
  `device_id` int NOT NULL,
  `device_type` varchar(10) NOT NULL,
  `network_mac_address` varchar(45) NOT NULL,
  `network_ip` varchar(20) NOT NULL,
  `allocate_id` int NOT NULL,
  `allocate_type` int NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL,
  PRIMARY KEY (`guest_network_id`)
) ENGINE=InnoDB AUTO_INCREMENT=133180 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_guest_password`
--

DROP TABLE IF EXISTS `tbl_guest_password`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_guest_password` (
  `guest_id` int NOT NULL,
  `encode_key` varchar(20) NOT NULL,
  `iv_key` varchar(20) NOT NULL,
  `guest_password` varchar(1024) NOT NULL,
  PRIMARY KEY (`guest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_guest_vnc`
--

DROP TABLE IF EXISTS `tbl_guest_vnc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_guest_vnc` (
  `guest_id` int NOT NULL,
  `vnc_port` int NOT NULL,
  `vnc_password` varchar(45) NOT NULL,
  `vnc_token` varchar(45) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`guest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_host_info`
--

DROP TABLE IF EXISTS `tbl_host_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_host_info` (
  `host_id` int NOT NULL AUTO_INCREMENT,
  `host_display_name` varchar(45) NOT NULL,
  `client_id` varchar(45) NOT NULL,
  `client_secret` varchar(45) NOT NULL,
  `host_name` varchar(45) NOT NULL DEFAULT '',
  `host_ip` varchar(20) NOT NULL,
  `host_nic_name` varchar(20) NOT NULL,
  `host_uri` varchar(128) NOT NULL,
  `host_allocation_memory` bigint NOT NULL,
  `host_allocation_cpu` int NOT NULL,
  `host_total_memory` bigint NOT NULL,
  `host_total_cpu` int NOT NULL,
  `host_arch` varchar(20) NOT NULL,
  `host_hypervisor` varchar(20) NOT NULL,
  `host_emulator` varchar(128) NOT NULL,
  `host_cpu_cores` int NOT NULL DEFAULT '0',
  `host_cpu_sockets` int NOT NULL DEFAULT '0',
  `host_cpu_threads` int NOT NULL DEFAULT '0',
  `host_status` int NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`host_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_meta_data`
--

DROP TABLE IF EXISTS `tbl_meta_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_meta_data` (
  `id` int NOT NULL AUTO_INCREMENT,
  `guest_id` int NOT NULL,
  `meta_key` varchar(128) NOT NULL,
  `meta_value` text NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `IX_GUEST_META` (`guest_id`,`meta_key`)
) ENGINE=InnoDB AUTO_INCREMENT=331 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_nat_info`
--

DROP TABLE IF EXISTS `tbl_nat_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_nat_info` (
  `nat_id` int NOT NULL AUTO_INCREMENT,
  `component_id` int NOT NULL,
  `nat_protocol` varchar(45) NOT NULL,
  `nat_local_port` int NOT NULL,
  `name_remote_ip` varchar(45) NOT NULL,
  `name_remote_port` int NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`nat_id`),
  UNIQUE KEY `IX_NAT` (`component_id`,`nat_local_port`,`nat_protocol`)
) ENGINE=InnoDB AUTO_INCREMENT=119 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_network_info`
--

DROP TABLE IF EXISTS `tbl_network_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_network_info` (
  `network_id` int NOT NULL AUTO_INCREMENT,
  `network_name` varchar(45) NOT NULL,
  `network_start_ip` varchar(20) NOT NULL,
  `network_stop_ip` varchar(20) NOT NULL,
  `network_gateway` varchar(20) NOT NULL,
  `network_mask` varchar(20) NOT NULL,
  `network_bridge_name` varchar(20) NOT NULL,
  `network_subnet` varchar(45) NOT NULL,
  `network_broadcast` varchar(45) DEFAULT NULL,
  `network_dns` varchar(128) NOT NULL,
  `network_vlan_id` int NOT NULL,
  `network_secret` varchar(128) NOT NULL DEFAULT 'CJ:KVM:CLOUD',
  `network_basic_network_id` int NOT NULL,
  `network_type` varchar(20) NOT NULL,
  `network_status` int NOT NULL,
  `network_domain` varchar(64) NOT NULL DEFAULT 'cj.kvm.internal',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`network_id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_scheme_info`
--

DROP TABLE IF EXISTS `tbl_scheme_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_scheme_info` (
  `scheme_id` int NOT NULL AUTO_INCREMENT,
  `scheme_name` varchar(45) NOT NULL,
  `scheme_cpu` int NOT NULL,
  `scheme_memory` bigint NOT NULL,
  `scheme_cpu_speed` int NOT NULL DEFAULT '0',
  `scheme_cpu_sockets` int NOT NULL DEFAULT '0',
  `scheme_cpu_cores` int NOT NULL DEFAULT '0',
  `scheme_cpu_threads` int NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`scheme_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_snapshot_volume`
--

DROP TABLE IF EXISTS `tbl_snapshot_volume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_snapshot_volume` (
  `snapshot_volume_id` int NOT NULL AUTO_INCREMENT,
  `snapshot_name` varchar(45) NOT NULL,
  `storage_id` int NOT NULL,
  `volume_name` varchar(45) NOT NULL,
  `volume_path` varchar(1024) NOT NULL,
  `volume_capacity` bigint NOT NULL,
  `volume_allocation` bigint NOT NULL,
  `volume_type` varchar(20) NOT NULL,
  `volume_status` int NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`snapshot_volume_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_storage_info`
--

DROP TABLE IF EXISTS `tbl_storage_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_storage_info` (
  `storage_id` int NOT NULL AUTO_INCREMENT,
  `storage_description` varchar(45) NOT NULL,
  `storage_name` varchar(64) NOT NULL,
  `storage_type` varchar(20) NOT NULL,
  `storage_parm` text NOT NULL,
  `storage_mount_path` varchar(1024) NOT NULL,
  `storage_capacity` bigint NOT NULL,
  `storage_available` bigint NOT NULL,
  `storage_allocation` bigint NOT NULL,
  `storage_status` int NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`storage_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_task_info`
--

DROP TABLE IF EXISTS `tbl_task_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_task_info` (
  `task_id` varchar(64) NOT NULL,
  `task_version` int NOT NULL,
  `task_title` varchar(255) DEFAULT NULL,
  `task_type` varchar(255) NOT NULL,
  `task_param` text NOT NULL,
  `create_time` timestamp NOT NULL,
  `expire_time` timestamp NOT NULL,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_template_info`
--

DROP TABLE IF EXISTS `tbl_template_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_template_info` (
  `template_id` int NOT NULL AUTO_INCREMENT,
  `template_name` varchar(45) NOT NULL,
  `template_uri` varchar(1024) NOT NULL,
  `template_type` int NOT NULL,
  `template_volume_type` varchar(10) NOT NULL,
  `template_status` int NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`template_id`)
) ENGINE=InnoDB AUTO_INCREMENT=67 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_template_volume`
--

DROP TABLE IF EXISTS `tbl_template_volume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_template_volume` (
  `template_volume_id` int NOT NULL AUTO_INCREMENT,
  `template_id` int NOT NULL,
  `storage_id` int NOT NULL,
  `template_name` varchar(45) NOT NULL,
  `template_path` varchar(1024) NOT NULL,
  `template_capacity` bigint NOT NULL,
  `template_allocation` bigint NOT NULL,
  `template_type` varchar(20) NOT NULL,
  `template_status` int NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`template_volume_id`)
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_user_info`
--

DROP TABLE IF EXISTS `tbl_user_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_user_info` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `login_name` varchar(10) NOT NULL,
  `login_password` varchar(64) NOT NULL COMMENT 'SHA_256(PWD+”:”+SALT)',
  `login_password_salt` varchar(45) NOT NULL COMMENT 'SHA2(concat("111111",":",login_password_salt),256)',
  `login_state` smallint NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `IX_LOGIN_NAME` (`login_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_volume_info`
--

DROP TABLE IF EXISTS `tbl_volume_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_volume_info` (
  `volume_id` int NOT NULL AUTO_INCREMENT,
  `volume_description` varchar(45) NOT NULL DEFAULT '',
  `template_id` int NOT NULL,
  `storage_id` int NOT NULL,
  `volume_name` varchar(45) NOT NULL,
  `volume_path` varchar(1024) NOT NULL,
  `volume_backing_path` varchar(1024) NOT NULL DEFAULT '',
  `volume_capacity` bigint NOT NULL,
  `volume_allocation` bigint NOT NULL,
  `volume_type` varchar(20) NOT NULL,
  `volume_status` int NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`volume_id`)
) ENGINE=InnoDB AUTO_INCREMENT=645 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-12-15  6:49:52



INSERT INTO`tbl_user_info`(`user_id`,`login_name`,`login_password`,`login_password_salt`,`login_state`,`create_time`)
VALUES(1,'admin','bf8ff699d7cf5dc1a85e0c143f61b093b60f86f932b0e232ee41314237635f0f','CRY:I0drTv3AlZLWYJ18',0,now());

insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_speed`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('7','1核512MB','1','524288','0','0','0','0','2022-12-28 10:51:27');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_speed`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('8','1核1GB','1','1048576','0','0','0','0','2022-12-28 10:51:27');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_speed`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('9','1核2GB','1','2097152','0','0','0','0','2022-12-28 10:51:27');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_speed`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('10','1核4GB','1','4194304','0','0','0','0','2022-12-28 10:51:27');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_speed`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('11','1核8GB','1','8388608','0','0','0','0','2022-12-28 10:51:27');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_speed`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('12','2核1GB','2','1048576','0','1','1','2','2022-12-28 10:51:27');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_speed`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('13','2核2GB','2','2097152','0','1','1','2','2022-12-28 10:51:27');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_speed`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('14','2核4GB','2','4194304','0','1','1','2','2022-12-28 10:51:27');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_speed`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('15','2核8GB','2','8388608','0','1','1','2','2021-05-18 03:23:26');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_speed`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('16','2核16GB','2','16777216','0','1','1','2','2021-05-18 03:23:26');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_speed`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('17','4核4G','4','4194304','0','2','1','2','2021-05-18 03:23:26');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_speed`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('18','4核8G','4','8388608','0','2','1','2','2021-05-18 03:23:26');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_speed`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('19','4核16G','4','16777216','0','2','1','2','2021-05-18 03:23:26');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_speed`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('21','8核16G','8','16777216','0','0','0','0','2021-05-18 03:23:26');

