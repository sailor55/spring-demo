package com.myspring.formwork.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author linjp
 * @version V1.0
 * @since 2020/3/4 9:09 下午
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestParam {
    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";
}
