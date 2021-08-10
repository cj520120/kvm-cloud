use roamblue_cloud_management;

CREATE TABLE `tbl_permission_category` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `category_name` varchar(45) COLLATE utf8mb4_bin NOT NULL COMMENT '分类名称',
  `category_sort` int(11) NOT NULL COMMENT '分类排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `tbl_permission_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `category_id` varchar(45) COLLATE utf8mb4_bin NOT NULL DEFAULT '0' COMMENT '分类ID',
  `permission_name` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '权限',
  `permission_description` varchar(64) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '权限说明',
  `permission_sort` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `IX_NAME` (`permission_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `tbl_rule_permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_name` varchar(45) COLLATE utf8mb4_bin NOT NULL,
  `group_permissions` text COLLATE utf8mb4_bin NOT NULL COMMENT '权限',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


ALTER TABLE `roamblue_cloud_management`.`tbl_login_info`
CHANGE COLUMN `rule_type` `rule_id` INT(11) NOT NULL ;