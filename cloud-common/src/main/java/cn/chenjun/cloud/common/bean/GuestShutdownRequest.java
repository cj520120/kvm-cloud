package cn.chenjun.cloud.common.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestShutdownRequest {
    /**
     * 虚拟机名称
     */
    private String name;

}