/*
Navicat MySQL Data Transfer

Source Server         : 192.168.2.107
Source Server Version : 50722
Source Host           : 192.168.2.107:3306
Source Database       : roamblue_kvm_cloud

Target Server Type    : MYSQL
Target Server Version : 50722
File Encoding         : 65001

Date: 2022-12-28 18:01:35
*/
CREATE DATABASE `roamblue_kvm_cloud` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
use roamblue_kvm_cloud;
SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for tbl_component_info
-- ----------------------------
DROP TABLE IF EXISTS `tbl_component_info`;
CREATE TABLE `tbl_component_info` (
  `component_id` int(11) NOT NULL AUTO_INCREMENT,
  `component_type` int(11) NOT NULL,
  `network_id` int(11) NOT NULL,
  `guest_id` int(11) NOT NULL,
  PRIMARY KEY (`component_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tbl_guest_disk
-- ----------------------------
DROP TABLE IF EXISTS `tbl_guest_disk`;
CREATE TABLE `tbl_guest_disk` (
  `guest_disk_id` int(11) NOT NULL AUTO_INCREMENT,
  `guest_id` int(11) NOT NULL,
  `volume_id` int(11) NOT NULL,
  `device_id` int(11) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`guest_disk_id`),
  UNIQUE KEY `IX_GUEST_VOLUME` (`guest_id`,`device_id`),
  UNIQUE KEY `IX_VOLUME_ID` (`volume_id`)
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tbl_guest_info
-- ----------------------------
DROP TABLE IF EXISTS `tbl_guest_info`;
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
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tbl_guest_network
-- ----------------------------
DROP TABLE IF EXISTS `tbl_guest_network`;
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
) ENGINE=InnoDB AUTO_INCREMENT=144 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tbl_guest_vnc
-- ----------------------------
DROP TABLE IF EXISTS `tbl_guest_vnc`;
CREATE TABLE `tbl_guest_vnc` (
  `guest_id` int(11) NOT NULL,
  `vnc_port` int(11) NOT NULL,
  `vnc_password` varchar(45) NOT NULL,
  `vnc_token` varchar(45) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`guest_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tbl_host_info
-- ----------------------------
DROP TABLE IF EXISTS `tbl_host_info`;
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

-- ----------------------------
-- Table structure for tbl_network_info
-- ----------------------------
DROP TABLE IF EXISTS `tbl_network_info`;
CREATE TABLE `tbl_network_info` (
  `network_id` int(11) NOT NULL AUTO_INCREMENT,
  `network_name` varchar(45) NOT NULL,
  `network_start_ip` varchar(20) NOT NULL,
  `network_stop_ip` varchar(20) NOT NULL,
  `network_gateway` varchar(20) NOT NULL,
  `network_mask` varchar(20) NOT NULL,
  `network_bridge_name` varchar(20) NOT NULL,
  `network_dns` varchar(20) NOT NULL,
  `network_vlan_id` int(11) NOT NULL,
  `network_basic_network_id` int(11) NOT NULL,
  `network_type` varchar(20) NOT NULL,
  `network_status` int(11) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`network_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tbl_scheme_info
-- ----------------------------
DROP TABLE IF EXISTS `tbl_scheme_info`;
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tbl_snapshot_volume
-- ----------------------------
DROP TABLE IF EXISTS `tbl_snapshot_volume`;
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
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tbl_storage_info
-- ----------------------------
DROP TABLE IF EXISTS `tbl_storage_info`;
CREATE TABLE `tbl_storage_info` (
  `storage_id` int(11) NOT NULL AUTO_INCREMENT,
  `storage_name` varchar(20) NOT NULL,
  `storage_type` varchar(20) NOT NULL,
  `storage_parm` text NOT NULL,
  `storage_mount_path` varchar(1024) NOT NULL,
  `storage_capacity` bigint(20) NOT NULL,
  `storage_available` bigint(20) NOT NULL,
  `storage_allocation` bigint(20) NOT NULL,
  `storage_status` int(11) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`storage_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tbl_template_info
-- ----------------------------
DROP TABLE IF EXISTS `tbl_template_info`;
CREATE TABLE `tbl_template_info` (
  `template_id` int(11) NOT NULL AUTO_INCREMENT,
  `template_name` varchar(45) NOT NULL,
  `template_uri` varchar(1024) NOT NULL,
  `template_type` int(11) NOT NULL,
  `template_volume_type` varchar(10) NOT NULL,
  `template_status` int(11) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`template_id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tbl_template_volume
-- ----------------------------
DROP TABLE IF EXISTS `tbl_template_volume`;
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
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for tbl_user_info
-- ----------------------------
DROP TABLE IF EXISTS `tbl_user_info`;
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

-- ----------------------------
-- Table structure for tbl_volume_info
-- ----------------------------
DROP TABLE IF EXISTS `tbl_volume_info`;
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
) ENGINE=InnoDB AUTO_INCREMENT=114 DEFAULT CHARSET=utf8mb4;
