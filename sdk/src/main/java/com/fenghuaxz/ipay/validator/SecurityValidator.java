package com.fenghuaxz.ipay.validator;

import com.fenghuaxz.ipay.annotations.SecurityCode;
import com.fenghuaxz.ipay.template.SecurityChecker;
import com.fenghuaxz.rpcframework.Hook;
import com.fenghuaxz.rpcframework.channels.Channel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class SecurityValidator extends Hook.AbstractMethodExecHook {

    @Override
    protected void beforeExec(Method method, Object[] parameters, Channel channel) throws Exception {
        for (int i = 0, j = method.getParameterTypes().length; i < j; i++) {
            Object parameter;
            if ((parameter = parameters[i]) == null || !(parameter instanceof String)) continue;
            boolean isVerify = false;
            Annotation[] annotations = method.getParameterAnnotations()[i];
            for (Annotation a : annotations) {
                isVerify = isVerify || a instanceof SecurityCode;
            }
            if (isVerify) {
                SecurityChecker checker = channel.template(SecurityChecker.class);
                if (!checker.check((String) parameter)) {
                    throw new IllegalStateException("Security check failed");
                }
            }
        }
    }

    @Override
    protected void afterExec(Method method, Channel channel, Object result) throws Exception {

    }
}
