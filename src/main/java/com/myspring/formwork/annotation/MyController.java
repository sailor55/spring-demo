package com.myspring.formwork.annotation;

import java.lang.annotation.*;

/**
 * @author linjp
 * @version V1.0
 * @since 2020/3/1 9:36 下午
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyController {
    String value() default "";
}
