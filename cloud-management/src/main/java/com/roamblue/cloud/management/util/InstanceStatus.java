package com.roamblue.cloud.management.util;

public class InstanceStatus {
    public final static String CREATING = "Creating";
    public final static String RUNING = "Running";
    public final static String STOPPED = "Stopped";
    public static final String DESTROY = "Destroy";
    public static final String ERROR = "Error";

    public static int getCompareValue(String status) {
        switch (status) {
            case CREATING:
                return 0;
            case RUNING:
                return 1;
            case STOPPED:
                return 2;
            case DESTROY:
                return 3;
            default:
                return 4;
        }
    }
}