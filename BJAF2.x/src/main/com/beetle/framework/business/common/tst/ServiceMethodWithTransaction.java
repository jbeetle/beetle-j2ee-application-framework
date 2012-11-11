package com.beetle.framework.business.common.tst;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//加载在VM中，在运行时进行映射
@Retention(RetentionPolicy.RUNTIME)
// 限定此annotation只能标示方法
@Target(ElementType.METHOD)
public @interface ServiceMethodWithTransaction {

}
