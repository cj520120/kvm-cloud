
CREATE TABLE IF NOT EXISTS `tbl_component_guest_info` (
                                            `component_guest_id` int NOT NULL AUTO_INCREMENT COMMENT '组件访客ID（主键）',
                                            `component_id` int NOT NULL COMMENT '组件ID',
                                            `component_type` int NOT NULL COMMENT '组件类型（1：路由，2：NAT）',
                                            `host_id` int NOT NULL COMMENT '主机ID',
                                            `guest_id` int NOT NULL COMMENT '访客ID（唯一）',
                                            `component_version` varchar(45) NOT NULL DEFAULT 'V1',
                                            `component_status` int NOT NULL COMMENT '组件状态',
                                            `session_id` varchar(45) NOT NULL,
                                            `error_count` int NOT NULL,
                                            `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `last_active_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                            PRIMARY KEY (`component_guest_id`) USING BTREE,
                                            UNIQUE KEY `uk_guest_id` (`guest_id`) USING BTREE,
                                            KEY `idx_component_id` (`component_id`) USING BTREE,
                                            KEY `idx_component_type` (`component_type`) USING BTREE
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='组件访客信息表';

CREATE TABLE IF NOT EXISTS `tbl_component_info` (
                                      `component_id` int NOT NULL AUTO_INCREMENT,
                                      `component_type` int NOT NULL,
                                      `component_vip` varchar(45) NOT NULL,
                                      `basic_component_vip` varchar(45) NOT NULL,
                                      `network_id` int NOT NULL,
                                      `create_time` timestamp NOT NULL,
                                      PRIMARY KEY (`component_id`),
                                      KEY `IX_NETWORK_COMPONENT` (`component_type`,`network_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tbl_route_strategy` (
                                      `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                      `component_id` varchar(64) NOT NULL COMMENT '组件/网络/设备唯一标识（用于索引查询）',
                                      `dest_ip` varchar(32) NOT NULL COMMENT '目标网段IP，如 192.168.50.0',
                                      `cidr` int NOT NULL COMMENT '子网掩码，如 24',
                                      `nexthop` varchar(32) NOT NULL COMMENT '下一跳IP（转发机器IP）',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_component_id` (`component_id`) COMMENT 'component_id 索引，加速查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='路由策略配置表';

CREATE TABLE IF NOT EXISTS `tbl_dns_info` (
                                `dns_id` int NOT NULL AUTO_INCREMENT,
                                `network_id` int NOT NULL,
                                `dns_domain` varchar(128) NOT NULL,
                                `dns_ip` varchar(45) NOT NULL,
                                `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`dns_id`),
                                UNIQUE KEY `IX_NETWORK_DOMAIN` (`network_id`,`dns_domain`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tbl_group_info` (
                                  `group_id` int NOT NULL AUTO_INCREMENT,
                                  `group_name` varchar(64) NOT NULL,
                                  `create_time` timestamp NOT NULL,
                                  PRIMARY KEY (`group_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `tbl_guest_info` (
                                  `guest_id` int NOT NULL AUTO_INCREMENT,
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
                                  `guest_arch` varchar(45) NOT NULL DEFAULT 'x86_64',
                                  `guest_uuid` varchar(45) NOT NULL,
                                  `guest_template_id` int NOT NULL DEFAULT '0',
                                  `group_id` int NOT NULL DEFAULT '0',
                                  `other_id` int NOT NULL,
                                  `guest_extern` text NOT NULL,
                                  `guest_status` int NOT NULL,
                                  `last_start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (`guest_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `tbl_guest_network` (
                                     `guest_network_id` int NOT NULL AUTO_INCREMENT,
                                     `network_id` int NOT NULL,
                                     `device_id` int NOT NULL,
                                     `device_type` varchar(10) NOT NULL,
                                     `network_mac_address` varchar(45) NOT NULL,
                                     `network_ip` varchar(20) NOT NULL,
                                     `allocate_id` int NOT NULL,
                                     `allocate_type` int NOT NULL DEFAULT '0',
                                     `allocate_description` varchar(64) NOT NULL DEFAULT '',
                                     `create_time` timestamp NOT NULL,
                                     PRIMARY KEY (`guest_network_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `tbl_host_info` (
                                 `host_id` int NOT NULL AUTO_INCREMENT,
                                 `host_display_name` varchar(45) NOT NULL,
                                 `client_id` varchar(45) NOT NULL,
                                 `client_secret` varchar(45) NOT NULL,
                                 `host_name` varchar(45) NOT NULL DEFAULT '',
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
                                 `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 PRIMARY KEY (`host_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `tbl_nat_info` (
                                `nat_id` int NOT NULL AUTO_INCREMENT,
                                `component_id` int NOT NULL,
                                `nat_protocol` varchar(45) NOT NULL,
                                `nat_local_port` int NOT NULL,
                                `name_remote_ip` varchar(45) NOT NULL,
                                `name_remote_port` int NOT NULL,
                                `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (`nat_id`),
                                UNIQUE KEY `IX_NAT` (`component_id`,`nat_local_port`,`nat_protocol`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `tbl_network_info` (
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
                                    `network_secret` varchar(128) NOT NULL DEFAULT 'RY:KVM:CLOUD',
                                    `network_basic_network_id` int NOT NULL,
                                    `network_type` varchar(20) NOT NULL,
                                    `network_status` int NOT NULL,
                                    `network_domain` varchar(64) NOT NULL DEFAULT 'ry.kvm.internal',
                                    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    PRIMARY KEY (`network_id`),
                                    UNIQUE KEY `IX_NETWORK_POOL_ID` (`network_pool_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `tbl_scheme_info` (
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
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `tbl_ssh_authorized_keys` (
                                           `id` int NOT NULL AUTO_INCREMENT,
                                           `ssh_name` varchar(45) NOT NULL,
                                           `ssh_public_key` text NOT NULL,
                                           `ssh_private_key` text NOT NULL,
                                           PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `tbl_storage_info` (
                                    `storage_id` int NOT NULL AUTO_INCREMENT,
                                    `storage_description` varchar(45) NOT NULL,
                                    `storage_name` varchar(64) NOT NULL,
                                    `storage_support_category` int NOT NULL DEFAULT '0',
                                    `storage_parent_id` int NOT NULL DEFAULT '0',
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
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `tbl_system_config` (
                                     `config_id` int NOT NULL AUTO_INCREMENT,
                                     `config_key` varchar(128) NOT NULL,
                                     `config_allocate_type` int NOT NULL,
                                     `config_allocate_id` int NOT NULL,
                                     `config_value` text NOT NULL,
                                     PRIMARY KEY (`config_id`),
                                     KEY `IX_ALLOCATE_CONFIG` (`config_allocate_type`,`config_allocate_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `tbl_task_info` (
                                 `task_id` varchar(64) NOT NULL,
                                 `task_version` int NOT NULL,
                                 `task_title` varchar(255) NOT NULL DEFAULT '',
                                 `task_type` varchar(255) NOT NULL,
                                 `task_param` text NOT NULL,
                                 `create_time` timestamp NOT NULL,
                                 `expire_time` timestamp NOT NULL,
                                 PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `tbl_template_info` (
                                     `template_id` int NOT NULL AUTO_INCREMENT,
                                     `template_name` varchar(128) NOT NULL,
                                     `template_uri` varchar(1024) NOT NULL,
                                     `template_arch` varchar(45) NOT NULL DEFAULT 'x86_64',
                                     `template_md5` varchar(45) NOT NULL DEFAULT '',
                                     `template_type` int NOT NULL,
                                     `cloud_wait_flag` int NOT NULL DEFAULT '0',
                                     `template_local_cloud_cfg` text NOT NULL COMMENT 'local 模式下需要注入的cloud配置',
                                     `template_vendor_data` text NOT NULL,
                                     `template_status` int NOT NULL,
                                     `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     PRIMARY KEY (`template_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `tbl_template_volume` (
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
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `tbl_user_info` (
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
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS `tbl_volume_info` (
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
                                   `device_type` varchar(10) NOT NULL DEFAULT 'disk',
                                   `device_driver` varchar(45) NOT NULL DEFAULT 'virtio',
                                   `volume_status` int NOT NULL,
                                   `volume_serial` varchar(45) NOT NULL,
                                   `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   PRIMARY KEY (`volume_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
