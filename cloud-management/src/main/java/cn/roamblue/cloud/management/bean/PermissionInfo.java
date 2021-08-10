package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: PermissionInfo
 * @Description: TODO
 * @Create by: chenjun
 * @Date: 2021/8/10 下午12:31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionInfo {
    /**
     * 权限ID
     */
    private int id;
    /**
     * 权限名称
     */
    private String name;
    /**
     * 权限说明
     */
    private String description;
    /**
     * 所属分类
     */
    private int categoryId;
    /**
     * 权限排序
     */
    private int sort;
}
