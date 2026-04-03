package cn.chenjun.cloud.management.filter;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.ClusterSignRequire;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.common.util.SecurityUtil;
import cn.chenjun.cloud.management.config.ApplicationConfig;
import cn.hutool.core.util.NumberUtil;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author chenjun
 */
@Component
public class ClusterSignInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private ApplicationConfig applicationConfig;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest httpServletRequest, @NonNull HttpServletResponse httpServletResponse, @NonNull Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            if (method.isAnnotationPresent(ClusterSignRequire.class)) {
                ClusterSignRequire require = method.getAnnotation(ClusterSignRequire.class);
                Map<String, Object> map = new HashMap<>(5);
                Enumeration<String> it = httpServletRequest.getParameterNames();
                while (it.hasMoreElements()) {
                    String key = it.nextElement();
                    map.put(key, httpServletRequest.getParameter(key));
                }
                String sign = (String) map.remove("sign");
                long timestamp = NumberUtil.parseLong((String) map.getOrDefault("timestamp", "0"));
                String secret = applicationConfig.getCluster().getToken();
                boolean isSuccess = false;
                long expire = timestamp + Objects.requireNonNull(require).timeout();
                String message = "成功";
                if (expire > System.currentTimeMillis()) {
                    try {
                        String dataSign = SecurityUtil.signature(map, secret);
                        if (Objects.equals(dataSign, sign)) {
                            isSuccess = true;
                        } else {
                            message = "签名不正确";
                        }
                    } catch (Exception e) {
                        message = "签名数据出错,请检查签名数据是否合法";
                    }
                } else {
                    message = "签名错误:签名时间验证失败,请确认服务器时间是否同步";
                }
                if (!isSuccess) {

                    httpServletResponse.setContentType("application/json; charset=utf-8");
                    httpServletResponse.setStatus(HttpStatus.OK.value());
                    httpServletResponse.getWriter().print(new Gson().toJson(ResultUtil.<Void>builder().code(ErrorCode.PERMISSION_ERROR).message(message).build()));
                    return false;
                }
            }
        }
        return true;
    }

}
