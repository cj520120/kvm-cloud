package cn.roamblue.cloud.management.aop;

import cn.roamblue.cloud.management.annotation.Rule;
import cn.roamblue.cloud.management.service.RuleService;
import cn.roamblue.cloud.management.util.RequestContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 验证用户是否有权限
 *
 * @author chenjun
 */
@Aspect
@Component
public class RuleAspect {
    @Autowired
    private RuleService ruleService;

    @Pointcut("@annotation(cn.roamblue.cloud.management.annotation.Rule)")
    public void rule() {
    }

    @Around("rule()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Rule rule = signature.getMethod().getAnnotation(Rule.class);
        if (rule != null) {
            ruleService.hasPermission(RequestContext.getCurrent().getUserId(), rule.min());
        }
        return joinPoint.proceed();
    }
}
