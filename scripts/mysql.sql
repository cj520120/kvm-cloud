/*
Navicat MySQL Data Transfer

Source Server         : 127.0.0.1
Source Server Version : 50722
Source Host           : 127.0.0.1:3306
Source Database       : cj_kvm_cloud

Target Server Type    : MYSQL
Target Server Version : 50722
File Encoding         : 65001

Date: 2022-12-28 18:01:35
*/
CREATE DATABASE `cj_kvm_cloud` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
use cj_kvm_cloud;
SET FOREIGN_KEY_CHECKS=0;

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
-- Table structure for table `tbl_component_info`
--

DROP TABLE IF EXISTS `tbl_component_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_component_info` (
  `component_id` int(11) NOT NULL AUTO_INCREMENT,
  `component_type` int(11) NOT NULL,
  `network_id` int(11) NOT NULL,
  `guest_id` int(11) NOT NULL,
  PRIMARY KEY (`component_id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_guest_disk`
--

DROP TABLE IF EXISTS `tbl_guest_disk`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_guest_disk` (
  `guest_disk_id` int(11) NOT NULL AUTO_INCREMENT,
  `guest_id` int(11) NOT NULL,
  `volume_id` int(11) NOT NULL,
  `device_id` int(11) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`guest_disk_id`),
  UNIQUE KEY `IX_GUEST_VOLUME` (`guest_id`,`device_id`),
  UNIQUE KEY `IX_VOLUME_ID` (`volume_id`)
) ENGINE=InnoDB AUTO_INCREMENT=103 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_guest_info`
--

DROP TABLE IF EXISTS `tbl_guest_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_guest_info` (
  `guest_id` int(11) NOT NULL AUTO_INCREMENT,
  `guest_name` varchar(45) NOT NULL,
  `guest_description` varchar(45) NOT NULL,
  `guest_bus_type` varchar(10) NOT NULL,
  `guest_cpu` int(11) NOT NULL,
  `guest_cpu_speed` int(11) NOT NULL DEFAULT '0',
  `guest_memory` bigint(20) NOT NULL,
  `guest_cd_room` int(11) NOT NULL,
  `host_id` int(11) NOT NULL,
  `last_host_id` int(11) NOT NULL,
  `scheme_id` int(11) NOT NULL DEFAULT '0',
  `guest_ip` varchar(48) NOT NULL DEFAULT '',
  `network_id` varchar(45) NOT NULL DEFAULT '0',
  `guest_type` int(11) NOT NULL,
  `guest_status` int(11) NOT NULL,
  `last_start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`guest_id`)
) ENGINE=InnoDB AUTO_INCREMENT=79 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_guest_network`
--

DROP TABLE IF EXISTS `tbl_guest_network`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_guest_network` (
  `guest_network_id` int(11) NOT NULL AUTO_INCREMENT,
  `guest_id` int(11) NOT NULL,
  `network_id` int(11) NOT NULL,
  `device_id` int(11) NOT NULL,
  `device_type` varchar(10) NOT NULL,
  `network_mac_address` varchar(45) NOT NULL,
  `network_ip` varchar(20) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`guest_network_id`)
) ENGINE=InnoDB AUTO_INCREMENT=389 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_guest_vnc`
--

DROP TABLE IF EXISTS `tbl_guest_vnc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_guest_vnc` (
  `guest_id` int(11) NOT NULL,
  `vnc_port` int(11) NOT NULL,
  `vnc_password` varchar(45) NOT NULL,
  `vnc_token` varchar(45) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`guest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_host_info`
--

DROP TABLE IF EXISTS `tbl_host_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_host_info` (
  `host_id` int(11) NOT NULL AUTO_INCREMENT,
  `host_display_name` varchar(45) NOT NULL,
  `client_id` varchar(45) NOT NULL,
  `client_secret` varchar(45) NOT NULL,
  `host_name` varchar(45) NOT NULL DEFAULT '',
  `host_ip` varchar(20) NOT NULL,
  `host_nic_name` varchar(20) NOT NULL,
  `host_uri` varchar(128) NOT NULL,
  `host_allocation_memory` bigint(20) NOT NULL,
  `host_allocation_cpu` int(11) NOT NULL,
  `host_total_memory` bigint(20) NOT NULL,
  `host_total_cpu` int(11) NOT NULL,
  `host_arch` varchar(20) NOT NULL,
  `host_hypervisor` varchar(20) NOT NULL,
  `host_emulator` varchar(128) NOT NULL,
  `host_cpu_cores` int(11) NOT NULL DEFAULT '0',
  `host_cpu_sockets` int(11) NOT NULL DEFAULT '0',
  `host_cpu_threads` int(11) NOT NULL DEFAULT '0',
  `host_status` int(11) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`host_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_network_info`
--

DROP TABLE IF EXISTS `tbl_network_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_network_info` (
  `network_id` int(11) NOT NULL AUTO_INCREMENT,
  `network_name` varchar(45) NOT NULL,
  `network_start_ip` varchar(20) NOT NULL,
  `network_stop_ip` varchar(20) NOT NULL,
  `network_gateway` varchar(20) NOT NULL,
  `network_mask` varchar(20) NOT NULL,
  `network_bridge_name` varchar(20) NOT NULL,
  `network_subnet` varchar(45) NOT NULL,
  `network_broadcast` varchar(45) DEFAULT NULL,
  `network_dns` varchar(20) NOT NULL,
  `network_vlan_id` int(11) NOT NULL,
  `network_basic_network_id` int(11) NOT NULL,
  `network_type` varchar(20) NOT NULL,
  `network_status` int(11) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`network_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_scheme_info`
--

DROP TABLE IF EXISTS `tbl_scheme_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_scheme_info` (
  `scheme_id` int(11) NOT NULL AUTO_INCREMENT,
  `scheme_name` varchar(45) NOT NULL,
  `scheme_cpu` int(11) NOT NULL,
  `scheme_memory` bigint(20) NOT NULL,
  `scheme_cpu_speed` int(11) NOT NULL DEFAULT '0',
  `scheme_cpu_sockets` int(11) NOT NULL DEFAULT '0',
  `scheme_cpu_cores` int(11) NOT NULL DEFAULT '0',
  `scheme_cpu_threads` int(11) NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`scheme_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_snapshot_volume`
--

DROP TABLE IF EXISTS `tbl_snapshot_volume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_snapshot_volume` (
  `snapshot_volume_id` int(11) NOT NULL AUTO_INCREMENT,
  `snapshot_name` varchar(45) NOT NULL,
  `storage_id` int(11) NOT NULL,
  `volume_name` varchar(45) NOT NULL,
  `volume_path` varchar(1024) NOT NULL,
  `volume_capacity` bigint(20) NOT NULL,
  `volume_allocation` bigint(20) NOT NULL,
  `volume_type` varchar(20) NOT NULL,
  `volume_status` int(11) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`snapshot_volume_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_storage_info`
--

DROP TABLE IF EXISTS `tbl_storage_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_storage_info` (
  `storage_id` int(11) NOT NULL AUTO_INCREMENT,
  `storage_description` varchar(45) NOT NULL,
  `storage_name` varchar(64) NOT NULL,
  `storage_type` varchar(20) NOT NULL,
  `storage_parm` text NOT NULL,
  `storage_mount_path` varchar(1024) NOT NULL,
  `storage_capacity` bigint(20) NOT NULL,
  `storage_available` bigint(20) NOT NULL,
  `storage_allocation` bigint(20) NOT NULL,
  `storage_status` int(11) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`storage_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_template_info`
--

DROP TABLE IF EXISTS `tbl_template_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_template_info` (
  `template_id` int(11) NOT NULL AUTO_INCREMENT,
  `template_name` varchar(45) NOT NULL,
  `template_uri` varchar(1024) NOT NULL,
  `template_type` int(11) NOT NULL,
  `template_volume_type` varchar(10) NOT NULL,
  `template_status` int(11) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`template_id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_template_volume`
--

DROP TABLE IF EXISTS `tbl_template_volume`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_template_volume` (
  `template_volume_id` int(11) NOT NULL AUTO_INCREMENT,
  `template_id` int(11) NOT NULL,
  `storage_id` int(11) NOT NULL,
  `template_name` varchar(45) NOT NULL,
  `template_path` varchar(1024) NOT NULL,
  `template_capacity` bigint(20) NOT NULL,
  `template_allocation` bigint(20) NOT NULL,
  `template_type` varchar(20) NOT NULL,
  `template_status` int(11) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`template_volume_id`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_user_info`
--

DROP TABLE IF EXISTS `tbl_user_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_user_info` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `login_name` varchar(10) NOT NULL,
  `login_password` varchar(64) NOT NULL COMMENT 'SHA_256(PWD+”:”+SALT)',
  `login_password_salt` varchar(45) NOT NULL COMMENT 'SHA2(concat("111111",":",login_password_salt),256)',
  `login_state` smallint(3) NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `IX_LOGIN_NAME` (`login_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tbl_volume_info`
--

DROP TABLE IF EXISTS `tbl_volume_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `tbl_volume_info` (
  `volume_id` int(11) NOT NULL AUTO_INCREMENT,
  `volume_description` varchar(45) NOT NULL DEFAULT '',
  `template_id` int(11) NOT NULL,
  `storage_id` int(11) NOT NULL,
  `volume_name` varchar(45) NOT NULL,
  `volume_path` varchar(1024) NOT NULL,
  `volume_capacity` bigint(20) NOT NULL,
  `volume_allocation` bigint(20) NOT NULL,
  `volume_type` varchar(20) NOT NULL,
  `volume_status` int(11) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`volume_id`)
) ENGINE=InnoDB AUTO_INCREMENT=121 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-01-05  5:59:09

DROP TABLE IF EXISTS `tbl_meta_data`;
CREATE TABLE `tbl_meta_data` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `guest_id` INT NOT NULL,
  `meta_key` VARCHAR(128) NOT NULL,
  `meta_value` TEXT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `IX_GUEST_META` (`guest_id` ASC, `meta_key` ASC));
INSERT INTO`cj_kvm_cloud`.`tbl_user_info`(`user_id`,`login_name`,`login_password`,`login_password_salt`,`login_state`,`create_time`)
VALUES(1,'admin','bf8ff699d7cf5dc1a85e0c143f61b093b60f86f932b0e232ee41314237635f0f','CRY:I0drTv3AlZLWYJ18',0,now());