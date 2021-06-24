package com.roamblue.cloud.management.util;

/**
 * @author chenjun
 */
public class VolumeStatus {
    public static final String READY = "Ready";
    public static final String DESTROY = "Destroy";
    public static final String TEMPLATE = "Template";

    public static int getCompareValue(String status) {
        switch (status) {
            case READY:
                return 0;
            case TEMPLATE:
                return 1;
            case DESTROY:
                return 2;
            default:
                return 3;
        }
    }
}
