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
public class PermissionCategory {
    /**
     * 权限ID
     */
    private int id;
    /**
     * 权限分类名称
     */
    private String name;
    /**
     * 权限分类排序
     */
    private int sort;
}
