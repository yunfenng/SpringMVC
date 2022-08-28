package com.lagou.edu.mvcframework.annotations;

import java.lang.annotation.*;

/**
 * @author Jaa
 * @date 2022/1/15 18:39
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LagouController {
    String value() default "";
}
