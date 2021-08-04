package cn.roamblue.cloud.management.filter;

import cn.roamblue.cloud.management.util.HttpHeaderNames;
import cn.roamblue.cloud.management.util.LocaleContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Locale;

/**
 * @ClassName: CloudLocaleResolver
 * @Description: TODO
 * @Create by: chenjun
 * @Date: 2021/8/4 下午1:43
 */
@Component
public class LocaleFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String val = req.getHeader(HttpHeaderNames.LANGUAGE);
        if (!StringUtils.isEmpty(val)) {
            LocaleContext.setLocale(new Locale(val));
        } else {
            LocaleContext.setLocale(Locale.getDefault());
        }
        chain.doFilter(request, response);
    }
}
