package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author DB1412
 * @create 2023-03-20 15:19
 * 自定义一个注解，注解在方法上，在运行时生效，用于实现拦截带有该注解的方法，从而实现未登录时不能随意访问页面
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {

}
