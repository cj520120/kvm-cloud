package cn.chenjun.cloud.management.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 * 用户实体类
 *
 * @author chenjun
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupModel {

    /**
     * 群组ID
     */
    private int groupId;
    /**
     * 群组名称
     */
    private String groupName;
    /**
     * 创建时间
     */
    private Date createTime;
}