package cn.roamblue.cloud.management.operate.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class InitHostNetworkOperate extends BaseOperateParam {
    private int networkId;
    private List<Integer> nextHostIds;
}
