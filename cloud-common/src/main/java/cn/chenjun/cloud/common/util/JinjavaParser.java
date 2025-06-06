package cn.chenjun.cloud.common.util;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenjun
 */
public class JinjavaParser {
    public static Jinjava create() {
        Jinjava jinjava = new Jinjava();
        jinjava.registerFunction(new ELFunctionDefinition("cloud", "parseUrlList", JinjavaParser.class, "parseUrlList", String.class, String.class));
        jinjava.registerFunction(new ELFunctionDefinition("cloud", "parseRandomFirstUri", JinjavaParser.class, "parseRandomFirstUri", String.class, String.class));
        jinjava.registerFunction(new ELFunctionDefinition("cloud", "toHex", JinjavaParser.class, "toHex", Integer.class, Integer.class));
        jinjava.registerFunction(new ELFunctionDefinition("cloud", "parseInteger", JinjavaParser.class, "parseInteger", Double.class));
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
        if (decimal == null || minLength == null || minLength <= 0) {
            throw new IllegalArgumentException("Invalid input");
        }

        String hexString = Integer.toHexString(decimal);
        if (hexString.length() >= minLength) {
            return "0x" + hexString;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("0x");
        for (int i = hexString.length(); i < minLength; i++) {
            sb.append("0");
        }
        sb.append(hexString);

        return sb.toString();
    }

    public static Integer parseInteger(Double value) {
        return value.intValue();
    }
}
