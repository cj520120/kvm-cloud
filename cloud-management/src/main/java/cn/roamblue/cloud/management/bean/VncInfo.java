package cn.roamblue.cloud.management.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * VNC信息
 *
 * @author chenjun
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VncInfo implements Serializable {

    /**
     * 主机地址
     */
    private String ip;
    /**
     * token
     */
    private String token;
    /**
     * password
     */
    private String password;
}
