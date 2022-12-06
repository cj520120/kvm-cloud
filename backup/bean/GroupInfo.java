package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 群组信息
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupInfo {

    /**
     * ID
     */
    private int id;
    /**
     * 名称
     */
    private String name;
    /**
     * 创建时间
     */
    private Date createTime;
}
