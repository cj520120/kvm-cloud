package cn.roamblue.cloud.management.aop;

import cn.roamblue.cloud.common.error.CodeException;
import cn.roamblue.cloud.common.util.ErrorCode;
import cn.roamblue.cloud.management.annotation.PreAuthority;
import cn.roamblue.cloud.management.service.RuleService;
import cn.roamblue.cloud.management.util.RequestContext;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 验证用户是否有权限
 *
 * @author chenjun
 */
@Aspect
@Component
public class AuthorityAspect {
    @Autowired
    private RuleService ruleService;

    @Pointcut("@annotation(cn.roamblue.cloud.management.annotation.PreAuthority)")
    public void preAuthority() {
    }

    @Around("preAuthority()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        PreAuthority preAuthority = signature.getMethod().getAnnotation(PreAuthority.class);
        if(!AuthorityValidator.verify(RequestContext.getCurrent().getSelf().getAuthorities(), preAuthority)){
            throw new CodeException(ErrorCode.PERMISSION_ERROR,"当前没有权限,如需操作，请联系管理员");
        }
        return joinPoint.proceed();
    }

    public static class AuthorityValidator {
        private final Collection<String> authorityList;

        public AuthorityValidator(Collection<String> authorityList) {
            this.authorityList = authorityList;
        }

        public boolean hasAuthority(String authority){
            if(StringUtils.isEmpty(authority)){
                return true;
            }
            return this.authorityList!=null&&this.authorityList.contains(authority);
        }
        public boolean hasAnyAuthority(Collection<String> authorities){
            if(authorityList.isEmpty()){
                return true;
            }
            for (String authority : authorities) {
                if(this.hasAuthority(authority)){
                    return true;
                }
            }
            return false;
        }
        public boolean hasAllAuthority(Collection<String> authorities){
            if(authorityList.isEmpty()){
                return true;
            }
            for (String authority : authorities) {
                if(!this.hasAuthority(authority)){
                    return false;
                }
            }
            return true;
        }
        public static boolean verify(Collection<String> authorityList, PreAuthority authority){
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(authority.value());
            return exp.getValue(new AuthorityValidator(authorityList),Boolean.class);
        }
    }
}
