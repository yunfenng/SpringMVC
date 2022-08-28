package com.lagou.edu.mvcframework.annotations;

import java.lang.annotation.*;

/**
 * @author Jaa
 * @date 2022/1/15 18:42
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LagouAutowired {
    String value() default "";
}
