package cn.roamblue.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

}
