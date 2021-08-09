package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 群组信息
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RulePermissionInfo {

    /**
     * ID
     */
    private int id;
    /**
     * 名称
     */
    private String name;
    /**
     * 权限
     */
    private List<String> permissions;
}
