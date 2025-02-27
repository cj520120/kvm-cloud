package cn.chenjun.cloud.management.util;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {
    public static <K, V> Map<K, V> of(K k1, V v1) {
        Map<K, V> map = new HashMap<>();
        map.put(k1, v1);
        return map;
    }
}
