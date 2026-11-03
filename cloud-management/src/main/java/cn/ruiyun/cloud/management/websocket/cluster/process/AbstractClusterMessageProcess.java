package cn.ruiyun.cloud.management.websocket.cluster.process;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.error.CodeException;
import cn.ruiyun.cloud.common.util.BeanConverter;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.management.servcie.LockRunner;
import cn.ruiyun.cloud.management.util.RedisKeyUtil;
import cn.ruiyun.cloud.management.websocket.message.NotifyData;
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

    protected <Source, Target> ResultUtil<Target> getResourceData(LockRunner.LockAction<Source> runnable, BeanConverter.Converter<Source, Target> converter) {
        try {
            Source result = lockRunner.lockCall(RedisKeyUtil.getGlobalLockKey(), runnable);
            Target data = converter.convert(result);
            return ResultUtil.<Target>builder().data(data).code(ErrorCode.SUCCESS).build();
        } catch (CodeException e) {
            return ResultUtil.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            throw new CodeException(ErrorCode.SERVER_ERROR, e);
        }
    }

}
