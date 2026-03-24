package cn.chenjun.cloud.management.util;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class ResourceUtil {
    public static String readUtf8Str(String resourcePath) {
        String resourceBody = cn.hutool.core.io.resource.ResourceUtil.readUtf8Str(resourcePath);
        try {
            ResourceContent resource = GsonBuilderUtil.create().fromJson(resourceBody, ResourceContent.class);
            if (!resource.getEncoding().equals("b64")) {
                throw new IllegalArgumentException("resource encoding must be b64");
            }
            byte[] contentBuffer = Base64.getDecoder().decode(resource.getContent().getBytes(StandardCharsets.UTF_8));
            String content = new String(contentBuffer, StandardCharsets.UTF_8);
            return content;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("读取资源失败，不是合法的base64数据，返回原是内容，path={}", resourcePath);
            return resourceBody;
        }
    }

    @Data
    public static class ResourceContent {
        private String content;
        private String encoding;
    }
}
