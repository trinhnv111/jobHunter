package trinhnv.springRestfull.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import trinhnv.springRestfull.common.annotation.ApiMessage;

@Aspect
@Component
public class ApiMessageAspect {

    @Around("@annotation(apiMessage)")
    public Object around(ProceedingJoinPoint joinPoint, ApiMessage apiMessage) throws Throwable {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        attributes.setAttribute("apiMessage", apiMessage.value(), RequestAttributes.SCOPE_REQUEST);
        return joinPoint.proceed();  // Không return ApiResponse ở đây
    }

}
