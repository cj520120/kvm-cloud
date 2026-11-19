package cn.ruiyun.cloud.agent.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SignatureReplayGuard {
    private static final Map<String, Long> NONCES = new ConcurrentHashMap<>();

    private SignatureReplayGuard() {
    }

    public static boolean isTimestampValid(long timestamp, long timeoutMillis) {
        long now = System.currentTimeMillis();
        return timestamp >= now - timeoutMillis && timestamp <= now + timeoutMillis;
    }

    public static boolean useNonce(String scope, String nonce, long timeoutMillis) {
        if (nonce == null || StringUtils.isBlank(scope)) {
            return false;
        }
        long now = System.currentTimeMillis();
        NONCES.entrySet().removeIf(entry -> entry.getValue() < now);
        return NONCES.putIfAbsent(scope + ':' + nonce, now + timeoutMillis) == null;
    }
}
