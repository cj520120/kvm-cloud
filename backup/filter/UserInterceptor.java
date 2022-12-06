package cn.roamblue.cloud.management.filter;

import cn.roamblue.cloud.management.bean.LoginUser;
import cn.roamblue.cloud.management.service.UserService;
import cn.roamblue.cloud.management.util.HttpHeaderNames;
import cn.roamblue.cloud.management.util.RequestContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author chenjun
 */
@Component
public class UserInterceptor extends HandlerInterceptorAdapter {


    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            RequestContext.remove();
            String token = httpServletRequest.getHeader(HttpHeaderNames.TOKEN_HEADER);
            if (!StringUtils.isEmpty(token)) {
                LoginUser userInfo = userService.getUserIdByToken(token);
                RequestContext.set(RequestContext.Context.builder().self(userInfo).build());
                httpServletRequest.setAttribute(HttpHeaderNames.LOGIN_USER_INFO_ATTRIBUTE, userInfo);
            }
        }
        return true;
    }

}
