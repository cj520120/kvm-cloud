CREATE DATABASE  IF NOT EXISTS `cj_kvm_cloud` /*!40100 DEFAULT CHARACTER SET utf8mb4 */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `cj_kvm_cloud`;
-- MySQL dump 10.13  Distrib 8.0.34, for macos13 (x86_64)
--
-- Host: 192.168.1.100    Database: cj_kvm_cloud
-- ------------------------------------------------------
-- Server version	8.0.41

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

--
-- Table structure for table `tbl_component_info`
--

DROP TABLE IF EXISTS `tbl_component_info`;

/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_component_info` (
  `component_id` int NOT NULL AUTO_INCREMENT,
  `component_type` int NOT NULL,
  `component_vip` varchar(45) NOT NULL,
  `basic_component_vip` varchar(45) NOT NULL,
  `network_id` int NOT NULL,
  `create_time` timestamp NOT NULL,
  PRIMARY KEY (`component_id`),
  KEY `IX_NETWORK_COMPONENT` (`component_type`,`network_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `tbl_dns_info`
--

DROP TABLE IF EXISTS `tbl_dns_info`;

/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_dns_info` (
  `dns_id` int NOT NULL AUTO_INCREMENT,
  `network_id` int NOT NULL,
  `dns_domain` varchar(128) NOT NULL,
  `dns_ip` varchar(45) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`dns_id`),
  UNIQUE KEY `IX_NETWORK_DOMAIN` (`network_id`,`dns_domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `tbl_group_info`
--

DROP TABLE IF EXISTS `tbl_group_info`;

/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_group_info` (
  `group_id` int NOT NULL AUTO_INCREMENT,
  `group_name` varchar(64) NOT NULL,
  `create_time` timestamp NOT NULL,
  PRIMARY KEY (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `tbl_guest_info`
--

DROP TABLE IF EXISTS `tbl_guest_info`;

/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_guest_info` (
  `guest_id` int NOT NULL AUTO_INCREMENT,
  `guest_uuid` varchar(45) NOT NULL,
  `system_category` int NOT NULL DEFAULT '100',
  `guest_name` varchar(45) NOT NULL,
  `guest_description` varchar(45) NOT NULL,
  `guest_cpu` int NOT NULL,
  `guest_cpu_share` int NOT NULL DEFAULT '0',
  `guest_memory` bigint NOT NULL,
  `guest_cd_room` int NOT NULL,
  `bind_host_id` int NOT NULL DEFAULT '0',
  `host_id` int NOT NULL,
  `last_host_id` int NOT NULL,
  `scheme_id` int NOT NULL DEFAULT '0',
  `guest_ip` varchar(48) NOT NULL DEFAULT '',
  `network_id` varchar(45) NOT NULL DEFAULT '0',
  `guest_type` int NOT NULL,
  `guest_bootstrap_type` int NOT NULL DEFAULT '0',
  `group_id` int NOT NULL DEFAULT '0',
  `other_id` int NOT NULL,
  `guest_extern` text NOT NULL,
  `guest_status` int NOT NULL,
  `last_start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`guest_id`),
  KEY `IX_UUID` (`guest_uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



--
-- Table structure for table `tbl_guest_network`
--

DROP TABLE IF EXISTS `tbl_guest_network`;

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
  `allocate_description` varchar(256) NOT NULL DEFAULT '',
  `create_time` timestamp NOT NULL,
  PRIMARY KEY (`guest_network_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `tbl_host_info`
--

DROP TABLE IF EXISTS `tbl_host_info`;

/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_host_info` (
  `host_id` int NOT NULL AUTO_INCREMENT,
  `host_display_name` varchar(45) NOT NULL,
  `client_id` varchar(45) NOT NULL,
  `client_secret` varchar(45) NOT NULL,
  `host_name` varchar(64) NOT NULL DEFAULT '',
  `host_os_name` varchar(64) NOT NULL,
  `host_os_version` varchar(64) NOT NULL,
  `host_ip` varchar(20) NOT NULL,
  `host_nic_name` varchar(20) NOT NULL,
  `host_uri` varchar(128) NOT NULL,
  `host_role` int NOT NULL DEFAULT '3',
  `host_allocation_memory` bigint NOT NULL,
  `host_allocation_cpu` int NOT NULL,
  `host_total_memory` bigint NOT NULL,
  `host_total_cpu` int NOT NULL,
  `host_cpu_model` varchar(128) NOT NULL,
  `host_cpu_frequency` bigint NOT NULL,
  `host_hypervisor` varchar(20) NOT NULL,
  `host_emulator` varchar(128) NOT NULL,
  `host_cpu_vendor` varchar(45) NOT NULL,
  `host_cpu_arch` varchar(20) NOT NULL,
  `host_cpu_cores` int NOT NULL DEFAULT '0',
  `host_cpu_sockets` int NOT NULL DEFAULT '0',
  `host_cpu_threads` int NOT NULL DEFAULT '0',
  `host_status` int NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`host_id`)
) ENGINE=InnoDB AUTO_INCREMENT=90 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



--
-- Table structure for table `tbl_nat_info`
--

DROP TABLE IF EXISTS `tbl_nat_info`;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `tbl_network_info`
--

DROP TABLE IF EXISTS `tbl_network_info`;

/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_network_info` (
  `network_id` int NOT NULL AUTO_INCREMENT,
  `network_pool_id` varchar(36) NOT NULL COMMENT 'MD5(UUID())',
  `network_name` varchar(45) NOT NULL,
  `network_start_ip` varchar(20) NOT NULL,
  `network_stop_ip` varchar(20) NOT NULL,
  `network_gateway` varchar(20) NOT NULL,
  `network_mask` varchar(20) NOT NULL,
  `network_bridge_name` varchar(20) NOT NULL,
  `network_bridge_type` int NOT NULL,
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
  PRIMARY KEY (`network_id`),
  UNIQUE KEY `IX_NETWORK_POOL_ID` (`network_pool_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `tbl_scheme_info`
--

DROP TABLE IF EXISTS `tbl_scheme_info`;

/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_scheme_info` (
  `scheme_id` int NOT NULL AUTO_INCREMENT,
  `scheme_name` varchar(45) NOT NULL,
  `scheme_cpu` int NOT NULL,
  `scheme_memory` bigint NOT NULL,
  `scheme_cpu_share` int NOT NULL DEFAULT '0',
  `scheme_cpu_sockets` int NOT NULL DEFAULT '0',
  `scheme_cpu_cores` int NOT NULL DEFAULT '0',
  `scheme_cpu_threads` int NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`scheme_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `tbl_ssh_authorized_keys`
--

DROP TABLE IF EXISTS `tbl_ssh_authorized_keys`;

/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_ssh_authorized_keys` (
  `id` int NOT NULL AUTO_INCREMENT,
  `ssh_name` varchar(45) NOT NULL,
  `ssh_public_key` text NOT NULL,
  `ssh_private_key` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `tbl_storage_info`
--

DROP TABLE IF EXISTS `tbl_storage_info`;

/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_storage_info` (
  `storage_id` int NOT NULL AUTO_INCREMENT,
  `storage_description` varchar(45) NOT NULL,
  `storage_name` varchar(64) NOT NULL,
  `storage_support_category` int NOT NULL DEFAULT '0',
  `storage_host_id` int NOT NULL,
  `storage_type` varchar(20) NOT NULL,
  `storage_parm` text NOT NULL,
  `storage_mount_path` varchar(1024) NOT NULL,
  `storage_capacity` bigint NOT NULL,
  `storage_available` bigint NOT NULL,
  `storage_allocation` bigint NOT NULL,
  `storage_status` int NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`storage_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `tbl_system_config`
--

DROP TABLE IF EXISTS `tbl_system_config`;

/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_system_config` (
  `config_id` int NOT NULL AUTO_INCREMENT,
  `config_key` varchar(128) NOT NULL,
  `config_allocate_type` int NOT NULL,
  `config_allocate_id` int NOT NULL,
  `config_value` text NOT NULL,
  PRIMARY KEY (`config_id`),
  KEY `IX_ALLOCATE_CONFIG` (`config_allocate_type`,`config_allocate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `tbl_task_info`
--

DROP TABLE IF EXISTS `tbl_task_info`;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `tbl_template_info`
--

DROP TABLE IF EXISTS `tbl_template_info`;

/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_template_info` (
  `template_id` int NOT NULL AUTO_INCREMENT,
  `template_name` varchar(45) NOT NULL,
  `template_uri` varchar(1024) NOT NULL,
  `template_md5` varchar(45) NOT NULL DEFAULT '',
  `template_type` int NOT NULL,
  `template_cloud_init_script` text NOT NULL,
  `template_status` int NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `tbl_template_volume`
--

DROP TABLE IF EXISTS `tbl_template_volume`;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `tbl_user_info`
--

DROP TABLE IF EXISTS `tbl_user_info`;

/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_user_info` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `user_name` varchar(45) NOT NULL,
  `login_name` varchar(10) NOT NULL,
  `login_password` varchar(64) NOT NULL COMMENT 'SHA_256(PWD+”:”+SALT)',
  `login_password_salt` varchar(45) NOT NULL COMMENT 'SHA2(concat("111111",":",login_password_salt),256)',
  `login_type` smallint NOT NULL,
  `user_type` smallint NOT NULL,
  `login_state` smallint NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `IX_LOGIN_NAME` (`login_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `tbl_volume_info`
--

DROP TABLE IF EXISTS `tbl_volume_info`;

/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tbl_volume_info` (
  `volume_id` int NOT NULL AUTO_INCREMENT,
  `volume_description` varchar(45) NOT NULL DEFAULT '',
  `template_id` int NOT NULL,
  `storage_id` int NOT NULL,
  `host_id` int NOT NULL,
  `volume_name` varchar(45) NOT NULL,
  `volume_path` varchar(1024) NOT NULL,
  `volume_capacity` bigint NOT NULL,
  `volume_allocation` bigint NOT NULL,
  `volume_type` varchar(20) NOT NULL,
  `guest_id` int NOT NULL DEFAULT '0',
  `device_id` int NOT NULL DEFAULT '0',
  `device_type` varchar(45) NOT NULL DEFAULT 'disk',
  `device_driver` varchar(45) NOT NULL DEFAULT 'virtio',
  `volume_status` int NOT NULL,
  `volume_serial` varchar(45) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`volume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;




INSERT INTO`tbl_user_info`(`user_id`,`user_name`,`login_name`,`login_password`,`login_password_salt`,`login_type`,`user_type`,`login_state`,`create_time`)
VALUES(1,'KVM Local Admin','admin','bf8ff699d7cf5dc1a85e0c143f61b093b60f86f932b0e232ee41314237635f0f','CRY:I0drTv3AlZLWYJ18',0,0,0,now());

insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_share`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('7','1核512MB','1','524288','0','0','0','0','2022-12-28 10:51:27');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_share`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('8','1核1GB','1','1048576','0','0','0','0','2022-12-28 10:51:27');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_share`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('9','1核2GB','1','2097152','0','0','0','0','2022-12-28 10:51:27');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_share`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('10','1核4GB','1','4194304','0','0','0','0','2022-12-28 10:51:27');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_share`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('11','1核8GB','1','8388608','0','0','0','0','2022-12-28 10:51:27');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_share`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('12','2核1GB','2','1048576','0','1','1','2','2022-12-28 10:51:27');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_share`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('13','2核2GB','2','2097152','0','1','1','2','2022-12-28 10:51:27');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_share`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('14','2核4GB','2','4194304','0','1','1','2','2022-12-28 10:51:27');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_share`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('15','2核8GB','2','8388608','0','1','1','2','2021-05-18 03:23:26');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_share`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('16','2核16GB','2','16777216','0','1','1','2','2021-05-18 03:23:26');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_share`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('17','4核4G','4','4194304','0','2','1','2','2021-05-18 03:23:26');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_share`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('18','4核8G','4','8388608','0','2','1','2','2021-05-18 03:23:26');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_share`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('19','4核16G','4','16777216','0','2','1','2','2021-05-18 03:23:26');
insert into `tbl_scheme_info` (`scheme_id`, `scheme_name`, `scheme_cpu`, `scheme_memory`, `scheme_cpu_share`, `scheme_cpu_sockets`, `scheme_cpu_cores`, `scheme_cpu_threads`, `create_time`) values('21','8核16G','8','16777216','0','0','0','0','2021-05-18 03:23:26');


