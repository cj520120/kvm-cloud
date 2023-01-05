package cn.chenjun.cloud.common.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenjun
 */
public class GsonBuilderUtil {
    public static Gson create() {
        GsonNumberAdapter gsonNumberAdapter = new GsonNumberAdapter();
        GsonBuilder gb = new GsonBuilder()
                .registerTypeAdapter(Map.class, gsonNumberAdapter)
                .registerTypeAdapter(HashMap.class, gsonNumberAdapter)
                .registerTypeAdapter(List.class, gsonNumberAdapter)
                .registerTypeAdapter(ArrayList.class, gsonNumberAdapter)
                .registerTypeAdapter(java.util.Date.class, new DateSerializer()).setDateFormat(DateFormat.LONG);
        return gb.create();
    }
}
