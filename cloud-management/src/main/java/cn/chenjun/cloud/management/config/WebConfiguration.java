package cn.chenjun.cloud.management.config;

import cn.chenjun.cloud.common.gson.DateSerializer;
import com.google.gson.GsonBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;

/**
 * @author chenjun
 */
@Configuration
@EnableConfigurationProperties
public class WebConfiguration {


    @Bean
    public GsonHttpMessageConverter gsonHttpMessageConverter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(java.util.Date.class, new DateSerializer());
        gsonBuilder.setDateFormat(DateFormat.LONG,DateFormat.DEFAULT);
        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gsonBuilder.create());
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        return converter;
    }

}
