package cn.chenjun.cloud.management.util;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
public class TemplateUtil {
    public static Jinjava create() {
        Jinjava jinjava = new Jinjava();
        jinjava.registerFunction(new ELFunctionDefinition("cloud", "parseUrlList", TemplateUtil.class, "parseUrlList", String.class, String.class));
        jinjava.registerFunction(new ELFunctionDefinition("cloud", "parseRandomFirstUri", TemplateUtil.class, "parseRandomFirstUri", String.class, String.class));
        jinjava.registerFunction(new ELFunctionDefinition("cloud", "toHex", TemplateUtil.class, "toHex", Integer.class, Integer.class));
        jinjava.registerFunction(new ELFunctionDefinition("cloud", "parseInteger", TemplateUtil.class, "parseInteger", Double.class));
        return jinjava;
    }

    public static List<Map<String, String>> parseUrlList(String uriListStr, String defaultPort) {
        List<String> uriList = Arrays.stream(uriListStr.split(",")).map(String::trim).filter(uri -> !ObjectUtils.isEmpty(uri)).collect(Collectors.toList());
        Collections.shuffle(uriList);
        List<Map<String, String>> hostList = new ArrayList<>();
        for (String uri : uriList) {
            String[] uriPort = uri.split(":");
            Map<String, String> map = new HashMap<>();
            if (uriPort.length == 1) {
                map.put("address", uriPort[0]);
                map.put("port", defaultPort);
            } else {
                map.put("address", uriPort[0]);
                map.put("port", uriPort[1]);
            }
            hostList.add(map);
        }
        return hostList;
    }

    public static Map<String, String> parseRandomFirstUri(String uriListStr, String defaultPort) {
        return parseUrlList(uriListStr, defaultPort).get(0);
    }

    public static String toHex(Integer decimal, Integer minLength) {
        String hexString = Integer.toHexString(decimal);
        while (hexString.length() < minLength) {
            hexString = "0" + hexString;
        }

        return "0x" + hexString;
    }

    public static Integer parseInteger(Double value) {
        return value.intValue();
    }
}
