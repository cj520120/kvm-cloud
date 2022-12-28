package cn.chenjun.cloud.agent.filter;

import cn.chenjun.cloud.agent.annotation.SignRequire;
import cn.chenjun.cloud.agent.util.ClientService;
import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.util.AppUtils;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.hutool.core.util.NumberUtil;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import java.util.concurrent.TimeUnit;

/**
 * @author chenjun
 */
@Component
public class SignInterceptor extends HandlerInterceptorAdapter {


    @Autowired
    private ClientService clientService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            if (method.isAnnotationPresent(SignRequire.class)) {
                SignRequire require = method.getAnnotation(SignRequire.class);
                Map<String, Object> map = new HashMap<>();
                Enumeration<String> it = httpServletRequest.getParameterNames();
                while (it.hasMoreElements()) {
                    String key = it.nextElement();
                    map.put(key, httpServletRequest.getParameter(key));

                }
                String sign = (String) map.remove("sign");
                String clientId = (String) map.get("clientId");
                String nonce = (String) map.get("nonce");
                long timestamp = NumberUtil.parseLong((String) map.getOrDefault("timestamp", "0"));

                long expire = timestamp + require.timeout();
                boolean isSuccess = false;
                if (expire > System.currentTimeMillis() && Objects.equals(clientService.getClientId(), clientId)) {
                    try {
                        String dataSign = AppUtils.sign(map, clientService.getClientId(), clientService.getClientSecret(), nonce);
                        if (Objects.equals(dataSign, sign)) {
                            isSuccess = true;
                        }
                    } catch (Exception e) {

                    }
                }
                if (!isSuccess) {

                    httpServletResponse.setContentType("application/json; charset=utf-8");
                    httpServletResponse.setStatus(HttpStatus.OK.value());
                    httpServletResponse.getWriter().print(new Gson().toJson(ResultUtil.<Void>builder().code(ErrorCode.SERVER_ERROR).message("签名错误").build()));
                    return false;
                }
            }
        }
        return true;
    }

}
