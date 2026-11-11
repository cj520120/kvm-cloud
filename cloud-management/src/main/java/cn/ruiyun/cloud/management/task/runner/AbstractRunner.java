package cn.ruiyun.cloud.management.task.runner;

import cn.ruiyun.cloud.management.servcie.ConfigService;
import cn.ruiyun.cloud.management.util.NotifyContextHolderUtil;
import cn.ruiyun.cloud.management.util.RequestContextHolderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author chenjun
 */
@Slf4j
public abstract class AbstractRunner {


    @Autowired
    protected ConfigService configService;
    private long lastRunTime = 0;


    public void call() {
        Long now = System.currentTimeMillis();
        long interval = this.getPeriodSeconds() * 1000L;
        if (now > interval + lastRunTime) {
            try {
                RequestContextHolderUtil.initContext();
                this.doWork();
            } catch (Exception err) {
                log.error("周期任务执行失败.", err);
            } finally {
                RequestContextHolderUtil.clearContext();
                NotifyContextHolderUtil.afterCompletion();
                this.lastRunTime = System.currentTimeMillis();
            }
        }
    }

    public void doWork() {
        try {
            log.debug("开始执行周期任务: {}，当前任务周期 {}s", this.getName(), this.getPeriodSeconds());
            this.dispatch();
        } catch (Exception err) {
            log.info("执行周期任务失败: {}", this.getName(), err);
        }
    }

    public int getPeriodSeconds() {
        return 10;
    }

    public int getDelaySeconds() {
        return 10;
    }


    /**
     * 任务分发
     *
     * @throws Exception
     */
    protected abstract void dispatch() throws Exception;

    /**
     * 任务名称
     *
     * @return
     */
    protected abstract String getName();


}
