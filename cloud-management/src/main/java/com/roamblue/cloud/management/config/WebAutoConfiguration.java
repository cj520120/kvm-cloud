package com.roamblue.cloud.management.config;

import com.google.gson.GsonBuilder;
import com.roamblue.cloud.management.filter.GsonDateSerializer;
import com.roamblue.cloud.management.filter.SpringfoxJsonToGsonAdapter;
import com.roamblue.cloud.management.util.HttpHeaderNames;
import com.roamblue.cloud.management.util.Version;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.json.Json;
import springfox.documentation.spring.web.plugins.Docket;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableConfigurationProperties
public class WebAutoConfiguration {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.setAllowCredentials(true);
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**", config);
        return new CorsFilter(configSource);
    }

    @Bean
    public GsonHttpMessageConverter gsonHttpMessageConverter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(java.util.Date.class, new GsonDateSerializer());
        gsonBuilder.registerTypeAdapter(Json.class, new SpringfoxJsonToGsonAdapter());
        gsonBuilder.setDateFormat(DateFormat.LONG);
        GsonHttpMessageConverter converter = new GsonHttpMessageConverter();
        converter.setGson(gsonBuilder.create());
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        return converter;
    }

    private Parameter initGlobalHeader(String name, String type, String description, String defaultValue) {
        return new ParameterBuilder().name(name).description(description).modelRef(new ModelRef(type)).parameterType("header").required(false).defaultValue(defaultValue).build();
    }

    @Bean
    public Docket docket() {
        List<Parameter> pars = new ArrayList<>();
        pars.add(this.initGlobalHeader(HttpHeaderNames.TOKEN_HEADER, "string", "Token信息", ""));

        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("Roamblue")
                .apiInfo(apiInfo("Cloud-Management"))
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(pars);
    }

    private ApiInfo apiInfo(String description) {
        return new ApiInfoBuilder()
                .title(description)
                .description(description)
                .version(Version.CURRENT_VERSION)
                .build();
    }
}
