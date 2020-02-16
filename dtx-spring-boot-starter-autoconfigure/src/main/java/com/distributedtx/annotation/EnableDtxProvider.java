package com.distributedtx.annotation;

import com.distributedtx.config.DtxImportSelecter;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({DtxImportSelecter.class})
public @interface EnableDtxProvider {
    /**
     * 示例 ： com.distributedtx
     * 则会扫描此包下的所有类和方法
     * 作用原理同 ComponentScan
     * @return
     */
    String dtxTransactionalScanPath() default "";
}
