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
public class VolumeSnapshotModel implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String tag;
    private Date createTime;
}
