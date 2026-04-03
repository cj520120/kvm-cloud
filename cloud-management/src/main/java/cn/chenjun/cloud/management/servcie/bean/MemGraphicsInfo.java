package cn.chenjun.cloud.management.servcie.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemGraphicsInfo {
    private int guestId;
    private String guestName;
    private String protocol;
    private int hostId;
    private String password;
}
