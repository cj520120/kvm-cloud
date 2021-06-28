package cn.roamblue.cloud.agent.config;

import cn.roamblue.cloud.agent.filter.DateSerializer;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;

/**
 * @author chenjun
 */
@Slf4j
@Configuration
@EnableConfigurationProperties
public class WebAutoConfiguration {


    @Bean
    public GsonHttpMessageConverter gsonHttpMessageConverter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(java.util.Date.class, new DateSerializer());
        gsonBuilder.setDateFormat(DateFormat.LONG);
        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gsonBuilder.create());
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        return converter;
    }

}
