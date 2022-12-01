package cn.roamblue.cloud.agent.util;

import cn.hutool.core.io.FileUtil;
import lombok.Synchronized;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Locale;
import java.util.UUID;

/**
 * @author chenjun
 */
public class HostUtil {

    private static String hostId;

    @Synchronized
    public static String getHostId() {
        if (StringUtils.isEmpty(hostId)) {
            File file = new File("HostId");
            if (!FileUtil.exist(file)) {
                FileUtil.writeString(UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT), file.getPath(), "UTF-8");
            }
            HostUtil.hostId = FileUtil.readUtf8String(file);
        }
        return hostId;
    }
}
