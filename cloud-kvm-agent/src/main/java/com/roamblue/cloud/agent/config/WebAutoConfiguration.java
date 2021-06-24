package com.roamblue.cloud.agent.config;

import com.google.gson.GsonBuilder;
import com.roamblue.cloud.agent.filter.DateSerializer;
import com.roamblue.cloud.agent.filter.SpringfoxJsonToGsonAdapter;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.json.Json;
import springfox.documentation.spring.web.plugins.Docket;

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
        gsonBuilder.registerTypeAdapter(Json.class, new SpringfoxJsonToGsonAdapter());
        gsonBuilder.setDateFormat(DateFormat.LONG);
        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gsonBuilder.create());
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        return converter;
    }

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("Roamblue")
                .apiInfo(apiInfo("Cloud-Agent"))
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo(String description) {
        return new ApiInfoBuilder()
                .title(description)
                .description(description)
                .version("1.0")
                .build();
    }
}
