package com.roamblue.cloud.management.util;

import java.util.UUID;

public class ServiceUtil {
    public static final String SERVICE_ID = UUID.randomUUID().toString().replace("-", "").toUpperCase();

}
