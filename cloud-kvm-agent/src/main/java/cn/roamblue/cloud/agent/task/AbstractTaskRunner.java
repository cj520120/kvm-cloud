package cn.roamblue.cloud.agent.task;

import cn.roamblue.cloud.agent.service.impl.ConnectPool;
import cn.roamblue.cloud.common.bean.ResultUtil;
import cn.roamblue.cloud.common.bean.TaskRequest;
import cn.roamblue.cloud.common.bean.TaskResponse;
import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.task.ScheduledExecutor;
import cn.roamblue.cloud.common.util.ErrorCode;
import org.libvirt.Connect;
import org.libvirt.Error;
import org.libvirt.LibvirtException;

/**
 * @author chenjun
 */
public abstract class AbstractTaskRunner<R, V> implements ScheduledExecutor.ScheduleRunner<TaskRequest<R>, V> {

    private final ConnectPool connectPool;

    public AbstractTaskRunner(ConnectPool connectPool) {
        this.connectPool = connectPool;
    }

    @Override
    public V run(TaskRequest<R> request) throws Throwable {
        Connect connect = null;
        try {
            connect = connectPool.borrowObject();
            return this.run(connect, request.getParam());
        } finally {
            if (connect != null) {
                connectPool.returnObject(connect);
            }
        }
    }


    /**
     * 执行任务
     *
     * @param connect
     * @param param
     * @return
     * @throws Throwable
     */
    protected abstract V run(Connect connect, R param) throws Throwable;

    @Override
    public void onScheduleFinish(boolean isSuccess, TaskRequest<R> request, V result, Throwable error) {
        ResultUtil<V> resultUtil = null;
        if (isSuccess) {
            resultUtil = ResultUtil.<V>builder().data(result).message("执行成功").build();
        } else {
            if (error instanceof LibvirtException) {
                LibvirtException err = (LibvirtException) error;
                if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_DOMAIN)) {
                    resultUtil = ResultUtil.<V>builder().code(ErrorCode.AGENT_VM_NOT_FOUND).message("domain not found or unexpectedly disappeared").build();
                } else if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_NETWORK)) {
                    resultUtil = ResultUtil.<V>builder().code(ErrorCode.QEMU_NOT_CONNECT).message("network not found").build();
                } else if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_CONNECT)) {
                    resultUtil = ResultUtil.<V>builder().code(ErrorCode.QEMU_NOT_CONNECT).message("can't connect to hypervisor").build();
                } else if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_STORAGE_POOL)) {
                    resultUtil = ResultUtil.<V>builder().code(ErrorCode.STORAGE_NOT_FOUND).message("storage not found").build();
                } else if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_NO_STORAGE_VOL)) {
                    resultUtil = ResultUtil.<V>builder().code(ErrorCode.VOLUME_NOT_FOUND).message("storage vol not found").build();
                } else if (err.getError().getCode().equals(Error.ErrorNumber.VIR_ERR_INVALID_STORAGE_VOL)) {
                    resultUtil = ResultUtil.<V>builder().code(ErrorCode.VOLUME_NOT_FOUND).message("invalid storage vol object").build();
                } else {
                    resultUtil = ResultUtil.<V>builder().code(ErrorCode.SERVER_ERROR).message(error.getMessage()).build();
                }
            } else if (error instanceof CodeException) {
                CodeException err = (CodeException) error;
                resultUtil = ResultUtil.<V>builder().code(err.getCode()).message(err.getMessage()).build();
            } else {
                resultUtil = ResultUtil.<V>builder().code(ErrorCode.SERVER_ERROR).message(error.getMessage()).build();
            }
        }
        TaskResponse response = TaskResponse.<V>builder().taskId(request.getTaskId()).result(result).code(resultUtil.getCode()).message(resultUtil.getMessage()).build();
        ScheduledExecutor.submit(new TaskResponseRunner(), response);
    }
}
