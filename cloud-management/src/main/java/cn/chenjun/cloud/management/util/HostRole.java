package cn.chenjun.cloud.management.util;

public class HostRole {
    public static final int MASTER = 1 << 0;
    public static final int WORK = 1 << 1;
    /**
     * 不指定
     */
    public static final int ALL = 0xFFFF;

    public static boolean isMaster(int role) {
        return (role & MASTER) == MASTER;
    }
}
