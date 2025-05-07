package cn.chenjun.cloud.management.filter;

import cn.chenjun.cloud.common.bean.ResultUtil;
import cn.chenjun.cloud.common.core.annotation.SignRequire;
import cn.chenjun.cloud.common.util.AppUtils;
import cn.chenjun.cloud.common.util.ErrorCode;
import cn.chenjun.cloud.management.data.entity.HostEntity;
import cn.chenjun.cloud.management.data.mapper.HostMapper;
import cn.chenjun.cloud.management.servcie.UserService;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

/**
 * @author chenjun
 */
@Component
public class SignInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private UserService userService;

    @Autowired
    private HostMapper hostMapper;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            if (method.isAnnotationPresent(SignRequire.class)) {
                SignRequire require = method.getAnnotation(SignRequire.class);
                Map<String, Object> map = new HashMap<>(5);
                Enumeration<String> it = httpServletRequest.getParameterNames();
                while (it.hasMoreElements()) {
                    String key = it.nextElement();
                    map.put(key, httpServletRequest.getParameter(key));

                }
                String sign = (String) map.remove("sign");
                String clientId = (String) map.get("clientId");
                String nonce = (String) map.get("nonce");
                long timestamp = NumberUtil.parseLong((String) map.getOrDefault("timestamp", "0"));
                HostEntity host = hostMapper.selectOne(new QueryWrapper<HostEntity>().eq(HostEntity.CLIENT_ID, clientId));
                boolean isSuccess = false;
                long expire = timestamp + require.timeout();
                String message = "成功";
                if (expire > System.currentTimeMillis()) {
                    try {
                        String dataSign = AppUtils.sign(map, clientId, host.getClientSecret(), nonce);
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
                    httpServletResponse.getWriter().print(new Gson().toJson(ResultUtil.<Void>builder().code(ErrorCode.SERVER_ERROR).message(message).build()));
                    return false;
                }
            }
        }
        return true;
    }

}
