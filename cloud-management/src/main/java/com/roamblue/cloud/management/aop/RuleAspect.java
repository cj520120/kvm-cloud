package com.roamblue.cloud.management.aop;

import com.roamblue.cloud.management.annotation.Rule;
import com.roamblue.cloud.management.service.RuleService;
import com.roamblue.cloud.management.util.RequestContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RuleAspect {
    @Autowired
    private RuleService ruleService;

    @Pointcut("@annotation(com.roamblue.cloud.management.annotation.Rule)")
    public void rule() {
    }

    @Around("rule()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Rule rule = signature.getMethod().getAnnotation(Rule.class);
        if (rule != null) {
            ruleService.verifyPermission(RequestContext.getCurrent().getUserId(), rule.min());
        }
        return joinPoint.proceed();
    }
}
