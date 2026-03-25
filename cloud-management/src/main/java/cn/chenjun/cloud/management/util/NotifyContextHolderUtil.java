package cn.chenjun.cloud.management.util;

import cn.chenjun.cloud.management.servcie.NotifyService;
import cn.chenjun.cloud.management.websocket.message.NotifyData;

import java.util.HashSet;
import java.util.Set;

public class NotifyContextHolderUtil {
    private final static ThreadLocal<Set<NotifyData>> NOTIFY_CONTEXT = new InheritableThreadLocal<>();

    public static <T> void append(NotifyData<T> notifyData) {
        Set<NotifyData> notifyDatas = NOTIFY_CONTEXT.get();
        if (notifyDatas == null) {
            notifyDatas = new HashSet<>();
            NOTIFY_CONTEXT.set(notifyDatas);
        }
        notifyDatas.add(notifyData);
    }

    public static void afterCompletion() {
        Set<NotifyData> notifyDatas = NOTIFY_CONTEXT.get();
        NotifyService notifyService = SpringContextUtils.getBean(NotifyService.class);
        if (notifyDatas != null) {
            notifyDatas.forEach(notifyService::publish);
            notifyDatas.clear();
            NOTIFY_CONTEXT.remove();
        }
    }
}
