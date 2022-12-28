package cn.chenjun.cloud.management.filter;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.annotation.LoginRequire;
import cn.chenjun.cloud.management.annotation.NoLoginRequire;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.servcie.UserService;
import cn.chenjun.cloud.management.util.RequestContext;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Component
public class AuthenticationInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private UserService userService;

    @Autowired
    private HostMapper hostMapper;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();

            boolean needLogin = method.isAnnotationPresent(LoginRequire.class);
            if (!needLogin && !method.isAnnotationPresent(NoLoginRequire.class)) {
                needLogin = method.getDeclaringClass().isAnnotationPresent(LoginRequire.class);
            }
            if (needLogin && null == RequestContext.getCurrent().getSelf()) {
                httpServletResponse.setContentType("application/json; charset=utf-8");
                httpServletResponse.setStatus(HttpStatus.OK.value());
                httpServletResponse.getWriter().print(new Gson().toJson(ResultUtil.<Void>builder().code(ErrorCode.NO_LOGIN_ERROR).build()));
                return false;
            }
        }
        return true;
    }

}
