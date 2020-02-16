package com.distributedtx.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 加了这个注解表示是一个分布式事物实体，即有 getMsgId 方法
 * 如果一个分布式事物方法中没有这个注解，则取第一个参数的 getMsgId 方法（没有则报错）
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DtxParam {
}
