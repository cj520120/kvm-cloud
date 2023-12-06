package cn.chenjun.cloud.agent.operate;

import cn.chenjun.cloud.common.bean.NoneRequest;
import org.libvirt.Connect;

import java.util.List;

public interface TaskService {
    List<String> checkTask(Connect connect, NoneRequest request);
}
