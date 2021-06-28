package cn.roamblue.cloud.management.service;

/**
 * @author chenjun
 */
public interface RuleService {
    /**
     * 验证用户权限
     *
     * @param userId
     * @param rule
     */
    void hasPermission(int userId, int rule);
}
