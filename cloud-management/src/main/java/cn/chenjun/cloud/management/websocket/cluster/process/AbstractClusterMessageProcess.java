package cn.chenjun.cloud.management.websocket.cluster.process;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.error.CodeException;
import cn.chenjun.cloud.common.util.BeanConverter;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.servcie.LockRunner;
import cn.chenjun.cloud.management.util.RedisKeyUtil;
import cn.chenjun.cloud.management.websocket.message.NotifyData;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chenjun
 */
public abstract class AbstractClusterMessageProcess<T> implements ClusterMessageProcess {
    @Autowired
    private LockRunner lockRunner;

    @Override
    public boolean supports(Integer type) {
        return type.equals(this.getType());
    }

    /**
     * 处理消息类型
     *
     * @return
     */
    protected abstract int getType();

    @Override
    public void process(NotifyData<?> msg) {
        if (msg.getType() == this.getType()) {
            this.doProcess((NotifyData<T>) msg);
        }
    }

    protected abstract void doProcess(NotifyData<T> msg);

    protected <S, T> ResultUtil<T> getResourceData(LockRunner.LockAction<S> runnable, BeanConverter.Converter<S, T> converter) {
        try {
            S result = lockRunner.lockCall(RedisKeyUtil.getGlobalLockKey(), runnable);
            T data = converter.convert(result);
            return ResultUtil.<T>builder().data(data).code(ErrorCode.SUCCESS).build();
        } catch (CodeException e) {
            return ResultUtil.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new CodeException(ErrorCode.SERVER_ERROR, e);
        }
    }

}
