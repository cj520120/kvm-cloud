use roamblue_cloud_management;
CREATE TABLE `tbl_rule_permission` (
  `id` int(11) NOT NULL,
  `group_name` varchar(45) COLLATE utf8mb4_bin NOT NULL,
  `group_permissions` text COLLATE utf8mb4_bin NOT NULL COMMENT '权限',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

ALTER TABLE `roamblue_cloud_management`.`tbl_login_info`
CHANGE COLUMN `rule_type` `rule_id` INT(11) NOT NULL ;