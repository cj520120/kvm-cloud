package cn.roamblue.cloud.management.operate.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNetworkOperate extends BaseOperateParam {
    private int networkId;
}
