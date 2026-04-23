package cn.chenjun.cloud.common.util;

import cn.chenjun.cloud.common.gson.GsonBuilderUtil;
import cn.hutool.core.lang.Assert;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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

    // ===================== 完全保留你原来的写法，只修复 JAR =====================
    @SneakyThrows
    public static List<ResourceContent> listResources(String basePath) {
        List<ResourceContent> list = new ArrayList<>();
        String searchPath = "classpath*:" + basePath + "/**";
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(searchPath);
        for (Resource resource : resources) {
            if (!resource.isReadable()) {
                continue;
            }
            URL url = resource.getURL();
            String urlStr = url.toString();
            int startIndex = urlStr.indexOf(basePath);
            Assert.isTrue(startIndex > 0, "Unexpected resource URL: " + urlStr);
            startIndex += basePath.length();
            int endIndex = urlStr.lastIndexOf('/');
            Assert.isTrue(endIndex > 0, "Unexpected resource URL: " + urlStr);
            String relativePath = urlStr.substring(startIndex, endIndex);
            try (InputStream is = resource.getInputStream()) {
                String body = new String(StreamUtils.copyToByteArray(is), StandardCharsets.UTF_8);
                ResourceContent content = GsonBuilderUtil.create().fromJson(body, ResourceContent.class);
                content.setPath(relativePath);
                list.add(content);
            }
        }
        return list;
    }

    @Data
    public static class ResourceContent {
        private String content;
        private String encoding;
        private String path;
        private String filename;
    }
}