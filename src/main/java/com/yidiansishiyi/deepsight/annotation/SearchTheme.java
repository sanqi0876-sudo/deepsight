package com.yidiansishiyi.deepsight.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SearchTheme {
    // 主题名称，比如 "入参实体"
    String name();
    // 主题唯一标识，比如 "parameter"
    String code();
}