package cn.roamblue.cloud.management.service;

import cn.roamblue.cloud.management.bean.PermissionCategory;
import cn.roamblue.cloud.management.bean.PermissionInfo;
import cn.roamblue.cloud.management.bean.RulePermissionInfo;

import java.util.List;

/**
 * @author chenjun
 */
public interface RuleService {


    /**
     * 权限检测
     * @param userId
     */
    List<String>  getUserPermissionList(int userId);

    /**
     * 获取所有权限列表
     * @return
     */
    List<PermissionInfo> listPermission();

    /**
     * 获取权限分组
     * @return
     */
    List<PermissionCategory> listPermissionCategory();

    /**
     * 获取权限组
     * @return
     */
    List<RulePermissionInfo> listRulePermission();

    /**
     * 创建权限组
     * @param name
     * @param permissions
     * @return
     */
    RulePermissionInfo createRulePermission(String name, String[] permissions);

    /**
     * 更新权限组
     * @param id
     * @param name
     * @param permissions
     * @return
     */
    RulePermissionInfo modifyRulePermission(int id, String name, String[] permissions);

    /**
     * 删除权限组
     * @param id
     */
    void destroyRulePermissionById(int id);
}
