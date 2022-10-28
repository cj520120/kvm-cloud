package cn.roamblue.cloud.common.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 虚拟机快照信息
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VmSnapshotModel implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 系统快照名称
     */
    private String name;
    /**
     * 系统快照描述
     */
    private String description;
    /**
     * 快照创建时间
     */
    private Date createTime;
}
