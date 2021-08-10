use roamblue_cloud_management;
UPDATE `tbl_login_info` SET `rule_id`='1';


-- ----------------------------
-- Records of tbl_permission_category
-- ----------------------------
INSERT INTO `tbl_permission_category` VALUES ('1', '集群管理', '1');
INSERT INTO `tbl_permission_category` VALUES ('2', '网络管理', '2');
INSERT INTO `tbl_permission_category` VALUES ('3', '主机管理', '3');
INSERT INTO `tbl_permission_category` VALUES ('4', '存储管理', '4');
INSERT INTO `tbl_permission_category` VALUES ('5', '模版管理', '5');
INSERT INTO `tbl_permission_category` VALUES ('6', '磁盘管理', '6');
INSERT INTO `tbl_permission_category` VALUES ('7', '计算方案管理', '7');
INSERT INTO `tbl_permission_category` VALUES ('8', '系统分类管理', '8');
INSERT INTO `tbl_permission_category` VALUES ('9', '权限管理', '9');
INSERT INTO `tbl_permission_category` VALUES ('10', 'VM管理', '10');
INSERT INTO `tbl_permission_category` VALUES ('11', '群组管理', '11');
INSERT INTO `tbl_permission_category` VALUES ('12', '用户管理', '12');

-- ----------------------------
-- Records of tbl_permission_info
-- ----------------------------
INSERT INTO `tbl_permission_info` VALUES ('1', '8', 'category.create', '系统分类创建', '101');
INSERT INTO `tbl_permission_info` VALUES ('2', '8', 'category.destroy', '系统分类销毁', '103');
INSERT INTO `tbl_permission_info` VALUES ('3', '8', 'category.modify', '系统分类修改', '102');
INSERT INTO `tbl_permission_info` VALUES ('4', '1', 'cluster.create', '集群创建', '201');
INSERT INTO `tbl_permission_info` VALUES ('5', '1', 'cluster.destroy', '集群销毁', '203');
INSERT INTO `tbl_permission_info` VALUES ('6', '1', 'cluster.modify', '集群修改', '202');
INSERT INTO `tbl_permission_info` VALUES ('7', '11', 'group.create', '群组创建', '301');
INSERT INTO `tbl_permission_info` VALUES ('8', '11', 'group.destroy', '群组销毁', '303');
INSERT INTO `tbl_permission_info` VALUES ('9', '11', 'group.modify', '群组修改', '302');
INSERT INTO `tbl_permission_info` VALUES ('10', '3', 'host.create', '主机创建', '401');
INSERT INTO `tbl_permission_info` VALUES ('11', '3', 'host.destroy', '主机销毁', '403');
INSERT INTO `tbl_permission_info` VALUES ('12', '3', 'host.status.modify', '主机修改', '402');
INSERT INTO `tbl_permission_info` VALUES ('13', '2', 'network.create', '网络创建', '501');
INSERT INTO `tbl_permission_info` VALUES ('14', '2', 'network.destroy', '网络销毁', '503');
INSERT INTO `tbl_permission_info` VALUES ('15', '2', 'network.status.modify', '网络状态变更', '502');
INSERT INTO `tbl_permission_info` VALUES ('16', '7', 'scheme.create', '计算方案创建', '601');
INSERT INTO `tbl_permission_info` VALUES ('17', '7', 'scheme.destroy', '计算方案销毁', '602');
INSERT INTO `tbl_permission_info` VALUES ('18', '4', 'storage.create', '存储池创建', '701');
INSERT INTO `tbl_permission_info` VALUES ('19', '4', 'storage.destroy', '存储池销毁', '702');
INSERT INTO `tbl_permission_info` VALUES ('20', '5', 'template.create', '模版创建', '801');
INSERT INTO `tbl_permission_info` VALUES ('21', '5', 'template.destroy', '模版销毁', '802');
INSERT INTO `tbl_permission_info` VALUES ('22', '12', 'user.destroy', '用户销毁', '1205');
INSERT INTO `tbl_permission_info` VALUES ('23', '12', 'user.password.reset', '用户重置密码', '1204');
INSERT INTO `tbl_permission_info` VALUES ('24', '12', 'user.permission.update', '用户权限修改', '1203');
INSERT INTO `tbl_permission_info` VALUES ('25', '12', 'user.register', '用户注册', '1201');
INSERT INTO `tbl_permission_info` VALUES ('26', '12', 'user.state.update', '用户状态变更', '1203');
INSERT INTO `tbl_permission_info` VALUES ('27', '10', 'vm.cd.update', 'VM挂载\\卸载光盘', '903');
INSERT INTO `tbl_permission_info` VALUES ('28', '10', 'vm.create', 'VM创建', '901');
INSERT INTO `tbl_permission_info` VALUES ('29', '10', 'vm.destroy', 'VM销毁', '910');
INSERT INTO `tbl_permission_info` VALUES ('30', '10', 'vm.disk.update', 'VM挂载\\卸载磁盘', '904');
INSERT INTO `tbl_permission_info` VALUES ('31', '10', 'vm.modify', 'VM信息修改', '902');
INSERT INTO `tbl_permission_info` VALUES ('32', '10', 'vm.nic.update', 'VM添加\\销毁网卡', '905');
INSERT INTO `tbl_permission_info` VALUES ('33', '10', 'vm.reinstall', 'VM重装', '906');
INSERT INTO `tbl_permission_info` VALUES ('34', '10', 'vm.resume', 'VM恢复', '909');
INSERT INTO `tbl_permission_info` VALUES ('35', '10', 'vm.status.update', 'VM启动、停止操作', '907');
INSERT INTO `tbl_permission_info` VALUES ('36', '10', 'vm.template', 'VM模版创建', '908');
INSERT INTO `tbl_permission_info` VALUES ('37', '6', 'volume.create', '磁盘创建', '1001');
INSERT INTO `tbl_permission_info` VALUES ('38', '6', 'volume.destroy', '磁盘销毁', '1003');
INSERT INTO `tbl_permission_info` VALUES ('39', '6', 'volume.resize', '磁盘扩容', '1002');
INSERT INTO `tbl_permission_info` VALUES ('40', '6', 'volume.resume', '磁盘恢复', '1004');
INSERT INTO `tbl_permission_info` VALUES ('41', '9', 'rule.permission.create', '权限组创建', '1101');
INSERT INTO `tbl_permission_info` VALUES ('42', '9', 'rule.permission.modify', '权限组修改', '1102');
INSERT INTO `tbl_permission_info` VALUES ('43', '9', 'rule.permission.destroy', '权限组销毁', '1103');



INSERT INTO `tbl_rule_permission` VALUES ('1', '超级管理员', 0x342C362C352C31332C31352C31342C31302C31322C31312C31382C31392C32302C32312C33372C33392C33382C34302C31362C31372C312C332C322C34312C34322C34332C32382C33312C32372C33302C33322C33332C33352C33362C33342C32392C372C392C382C32352C32362C32342C32332C3232);
