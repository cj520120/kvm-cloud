package com.roamblue.cloud.management.util;

public class LockKeyUtil {
    public static String getInstanceLockKey(int id) {
        return "Instance." + id;
    }

    public static String getTemplateLockKey(int id) {
        return "Template." + id;
    }

    public static String getVolumeLockKey(int id) {
        return "Volume." + id;
    }

    public static String getStorageLockKey(int id) {
        return "Storage." + id;
    }

    public static String getClusterLockKey(int id) {
        return "Cluster." + id;
    }

    public static String getVncLock(int id) {
        return "Vnc." + id;
    }

    public static String getRouteLock(int id) {
        return "Route." + id;
    }

    public static String getHostLock(int id) {
        return "Host." + id;
    }

    public static String getTaskLock(String task) {
        return "Task." + task;
    }
}
