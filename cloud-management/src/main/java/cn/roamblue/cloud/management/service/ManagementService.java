package cn.roamblue.cloud.management.service;

/**
 * @author chenjun
 */
public interface ManagementService {
    /**
     * 管理端保活
     */
    void keep();

    /**
     * 尝试获取任务锁
     *
     * @param name
     * @return
     */
    boolean applyTask(String name);
}
