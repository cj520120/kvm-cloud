package cn.roamblue.cloud.management.operate.bean;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author chenjun
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DestroyHostStorageOperate extends BaseOperateParam {
    private int storageId;
    private List<Integer> nextHostIds;
}
