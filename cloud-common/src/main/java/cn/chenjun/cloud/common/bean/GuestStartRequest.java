package cn.chenjun.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestStartRequest {
    private String name;
    private String description;
    private String emulator;
    private OsMemory osMemory;
    private OsCpu osCpu;
    private OsCdRoom osCdRoom;
    private String bus;
    private List<OsDisk> osDisks;
    private List<OsNic> networkInterfaces;
    private String vncPassword;
    private GuestQmaRequest qmaRequest;
    private int systemCategory;
    private int bootstrapType;

}
