package cn.roamblue.cloud.management.ui;

import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.management.bean.PermissionCategory;
import cn.roamblue.cloud.management.bean.PermissionInfo;
import cn.roamblue.cloud.management.bean.RulePermissionInfo;

import java.util.List;

/**
 * @ClassName: RuleUiService
 * @Description: TODO
 * @Create by: chenjun
 * @Date: 2021/8/9 下午5:43
 */
public interface RuleUiService {
    /**
     * 查询权限组
     * @return
     */
    ResultUtil<List<RulePermissionInfo>> listRulePermission();

    /**
     * 获取所有权限列表
     * @return
     */
    ResultUtil<List<PermissionInfo>> listPermission();

    /**
     * 获取权限分类
     * @return
     */
    ResultUtil<List<PermissionCategory>> listPermissionCategory();

    /**
     * 创建权限组
     * @param name
     * @param permissions
     * @return
     */
    ResultUtil<RulePermissionInfo>createRulePermission(String name, String[] permissions);


    /**
     * 更新权限组
     * @param id
     * @param name
     * @param permissions
     * @return
     */
    ResultUtil<RulePermissionInfo>modifyRulePermission(int id, String name, String[] permissions);

    /**
     * 删除权限组
     * @param id
     * @return
     */
    ResultUtil<Void> destroyRulePermissionById(int id);
}
