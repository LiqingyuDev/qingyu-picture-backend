package com.qingyu.qingyupicturebackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 *
 * 该注解用于标记需要进行权限校验的方法。当方法被标记了此注解后，
 * 在调用该方法之前会检查当前用户是否具有指定的角色权限。
 *
 * @author liqingyu.dev@gmail.com
 * @createDate 2024/12/10 下午3:42
 */
@Target(ElementType.METHOD) // 指定注解可以应用于方法上
@Retention(RetentionPolicy.RUNTIME) // 指定这个注解在运行时可以被反射读取
public @interface AuthCheck {

    /**
     * 必须有该角色
     *
     * 该属性指定了调用方法时用户必须具有的角色。如果用户没有指定的角色，
     * 则会抛出权限不足的异常。
     *
     * @return 角色名称，默认为空字符串
     */
    String mustRole() default "";
}
