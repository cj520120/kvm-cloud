package com.roamblue.cloud.agent.filter;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * @author chenjun
 */
public class DateSerializer implements JsonSerializer<Date>, JsonDeserializer<Date> {

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getTime());
    }

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return new Date(json.getAsJsonPrimitive().getAsLong());
    }
}