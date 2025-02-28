package cn.chenjun.cloud.agent.util;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.lib.fn.ELFunctionDefinition;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author chenjun
 */
public class TemplateUtil {
    public static Jinjava create() {
        Jinjava jinjava = new Jinjava();
        jinjava.registerFunction(new ELFunctionDefinition("cloud", "split", TemplateUtil.class, "splitToList", String.class, String.class));
        return jinjava;
    }

    public static List<String> splitToList(String str, String split) {
        if (StringUtils.isEmpty(str)) {
            return new ArrayList<>();
        }
        String[] temp = str.split(split);
        return Arrays.asList(temp);
    }
}
