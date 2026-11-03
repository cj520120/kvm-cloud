package cn.ruiyun.cloud.management.filter;

import cn.ruiyun.cloud.common.bean.ResultUtil;
import cn.ruiyun.cloud.common.core.annotation.LoginRequire;
import cn.ruiyun.cloud.common.core.annotation.NoLoginRequire;
import cn.ruiyun.cloud.common.util.ErrorCode;
import cn.ruiyun.cloud.management.model.LoginUserModel;
import cn.ruiyun.cloud.management.servcie.UserService;
import cn.ruiyun.cloud.management.util.RequestContextHolderUtil;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
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
    public boolean preHandle(@NonNull HttpServletRequest httpServletRequest, @NonNull HttpServletResponse httpServletResponse, @NonNull Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();

            boolean needLogin = method.isAnnotationPresent(LoginRequire.class);
            if (!needLogin && !method.isAnnotationPresent(NoLoginRequire.class)) {
                needLogin = method.getDeclaringClass().isAnnotationPresent(LoginRequire.class);
            }
            LoginUserModel loginUserModel = RequestContextHolderUtil.get(RequestContextHolderUtil.CURRENT_USER);
            if (needLogin && loginUserModel == null) {
                httpServletResponse.setContentType("application/json; charset=utf-8");
                httpServletResponse.setStatus(HttpStatus.OK.value());
                httpServletResponse.getWriter().print(new Gson().toJson(ResultUtil.<Void>builder().code(ErrorCode.NO_LOGIN_ERROR).build()));
                return false;
            }
        }
        return true;
    }

}
