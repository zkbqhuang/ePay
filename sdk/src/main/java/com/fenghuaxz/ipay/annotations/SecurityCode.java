package com.fenghuaxz.ipay.annotations;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityCode {
}
