package cn.roamblue.cloud.management.util;

/**
 * @author chenjun
 */
public class VmStatus {
    public final static String CREATING = "Creating";
    public final static String RUNNING = "Running";
    public final static String STOPPED = "Stopped";
    public static final String DESTROY = "Destroy";
    public static final String ERROR = "Error";
    public final static String STARING = "Starting";

    public static int getCompareValue(String status) {
        switch (status) {
            case STARING:
                return 0;
            case CREATING:
                return 1;
            case RUNNING:
                return 2;
            case STOPPED:
                return 3;
            case DESTROY:
                return 4;
            default:
                return 5;
        }
    }
}