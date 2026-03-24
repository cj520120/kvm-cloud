package cn.chenjun.cloud.common.util;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {
    public static <K, V> Map<K, V> of(Object... args) {
        Map<K, V> map = new HashMap<>();
        int length = args.length;
        if (length % 2 != 0) {
            throw new IllegalArgumentException("args length must be even");
        }
        for (int i = 0; i < length; i += 2) {
            map.put((K) args[i], (V) args[i + 1]);
        }
        return map;
    }
}
