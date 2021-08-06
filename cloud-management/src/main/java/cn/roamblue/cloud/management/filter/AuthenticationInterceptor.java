package cn.roamblue.cloud.management.filter;

import cn.roamblue.cloud.management.annotation.Login;
import cn.roamblue.cloud.management.service.UserService;
import cn.roamblue.cloud.management.util.HttpHeaderNames;
import cn.roamblue.cloud.management.util.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @author chenjun
 */
@Component
public class AuthenticationInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        String token = httpServletRequest.getHeader(HttpHeaderNames.TOKEN_HEADER);
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            if (method.isAnnotationPresent(Login.class)) {
                Login userLoginToken = method.getAnnotation(Login.class);
                if (userLoginToken.required()) {
                    Integer userId = userService.verify(token);
                    RequestContext.set(RequestContext.Context.builder().userId(userId).build());
                    httpServletRequest.setAttribute(HttpHeaderNames.LOGIN_USER_ID_ATTRIBUTE, userId);
                }
            }
        }
        return true;
    }

}
