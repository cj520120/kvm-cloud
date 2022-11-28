package cn.roamblue.cloud.agent.task;

import cn.roamblue.cloud.agent.service.impl.ConnectPool;
import cn.roamblue.cloud.agent.operate.StorageOperate;
import cn.roamblue.cloud.common.agent.StorageModel;
import cn.roamblue.cloud.common.agent.StorageRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Command;
import cn.roamblue.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;

import java.util.ServiceLoader;

/**
 * @author chenjun
 */
@Slf4j
public class StorageRunner extends AbstractTaskRunner<StorageRequest, StorageModel> {

    private final ServiceLoader<StorageOperate> loader = ServiceLoader.load(StorageOperate.class);

    public StorageRunner(ConnectPool connectPool) {
        super(connectPool);
    }

    @Override
    protected StorageModel run(Connect connect, StorageRequest request) throws Exception {

        if (Command.Storage.CREATE.equals(request.getCommand())) {

        } else if (Command.Storage.DESTROY.equals(request.getCommand())) {

        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的存储池操作:" + request.getCommand());
        }
        return null;
    }
}
