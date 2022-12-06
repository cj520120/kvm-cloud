package cn.roamblue.cloud.management.util;

import java.util.UUID;

/**
 * @author chenjun
 */
public class ServiceId {
    public static final String CURRENT_SERVICE_ID = UUID.randomUUID().toString().replace("-", "").toUpperCase();

}
