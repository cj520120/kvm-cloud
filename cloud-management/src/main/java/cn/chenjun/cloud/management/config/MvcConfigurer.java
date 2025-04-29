package cn.chenjun.cloud.management.config;

import cn.chenjun.cloud.management.filter.AuthenticationInterceptor;
import cn.chenjun.cloud.management.filter.PermissionInterceptor;
import cn.chenjun.cloud.management.filter.SignInterceptor;
import cn.chenjun.cloud.management.filter.UserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * @author chenjun
 */
@Configuration
public class MvcConfigurer implements WebMvcConfigurer {
    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;
    @Autowired
    private UserInterceptor userInterceptor;
    @Autowired
    private SignInterceptor signInterceptor;
    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(authenticationInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(permissionInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(signInterceptor).addPathPatterns("/api/agent/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/META-INF/resources/")
                .addResourceLocations("classpath:/static/");

    }

}
