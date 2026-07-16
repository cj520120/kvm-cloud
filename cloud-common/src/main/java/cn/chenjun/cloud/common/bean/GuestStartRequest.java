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
    private String xml;
    private List<NetworkNic> nics;
    private boolean waitCloudInit;
    private int waitCloudInitTimeoutSeconds;

}
