package com.example.demo.annotation;

import com.example.demo.utils.CacheKey;

import java.lang.annotation.*;

/**
 * Created by fupeng-ds on 2018/6/28.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Cacheable {

    String key();

    String field() default "";

    int expire() default 600;

    CacheKey type() default CacheKey.CACHE;

}
