package cn.chenjun.cloud.management.util;

public class HostRole {
    public static final int MASTER = 1 << 0;
    public static final int WORK = 1 << 1;
    /**
     * 不指定
     */
    public static final int NONE = 0x0;

    public static boolean isMaster(int role) {
        return (role & MASTER) == MASTER;
    }
    public static boolean hasRole(int role,int checkRole) {
        return (role & checkRole) == checkRole;
    }
}
