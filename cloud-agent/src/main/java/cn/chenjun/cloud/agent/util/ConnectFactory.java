package cn.chenjun.cloud.agent.util;

import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;

/**
 * @author chenjun
 */
@Slf4j
public class ConnectFactory {
    public static Connect create() throws Exception {
        return new Connect("qemu:///system");
    }

}
