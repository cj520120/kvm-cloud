package cn.chenjun.cloud.management.filter;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.model.LoginUserModel;
import cn.chenjun.cloud.management.servcie.UserService;
import cn.chenjun.cloud.management.util.Constant;
import cn.chenjun.cloud.management.util.RequestContext;
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
            String token = httpServletRequest.getHeader(Constant.HttpHeaderNames.TOKEN_HEADER);
            if (!StringUtils.isEmpty(token)) {
                try {
                    ResultUtil<LoginUserModel> resultUtil = userService.getUserIdByToken(token);
                    if (resultUtil.getCode() == ErrorCode.SUCCESS) {
                        RequestContext.set(RequestContext.Context.builder().self(resultUtil.getData()).build());
                        httpServletRequest.setAttribute(Constant.HttpHeaderNames.LOGIN_USER_INFO_ATTRIBUTE, resultUtil.getData());
                    }
                } catch (Exception err) {

                }
            }
        }
        return true;
    }

}
