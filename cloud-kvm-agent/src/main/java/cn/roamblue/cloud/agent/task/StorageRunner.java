package cn.roamblue.cloud.agent.task;

import cn.roamblue.cloud.agent.operate.StorageOperate;
import cn.roamblue.cloud.agent.operate.impl.StorageOperateImpl;
import cn.roamblue.cloud.agent.service.impl.ConnectPool;
import cn.roamblue.cloud.common.agent.StorageModel;
import cn.roamblue.cloud.common.agent.StorageRequest;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.Command;
import cn.roamblue.cloud.common.util.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.libvirt.Connect;

/**
 * @author chenjun
 */
@Slf4j
public class StorageRunner extends AbstractTaskRunner<StorageRequest, StorageModel> {

    private final StorageOperate storageOperate = new StorageOperateImpl();

    public StorageRunner(ConnectPool connectPool) {
        super(connectPool);
    }

    @Override
    protected StorageModel run(Connect connect, StorageRequest request) throws Exception {

        if (Command.Storage.CREATE.equals(request.getCommand())) {
            return storageOperate.create(connect, request);
        } else if (Command.Storage.DESTROY.equals(request.getCommand())) {
            storageOperate.destroy(connect, request.getName());
        } else {
            throw new CodeException(ErrorCode.SERVER_ERROR, "不支持的存储池操作:" + request.getCommand());
        }
        return null;
    }
}
