package com.lagou.edu.mvcframework.annotations;

import java.lang.annotation.*;

/**
 * @author Jaa
 * @date 2022/1/15 18:42
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LagouRequestMapping {
    String value() default "";
}
