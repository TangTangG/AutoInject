package com.todo.autocollect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by TCG on 2018/7/6.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Collector {

    /**
     * The annotation class type wanted to collected.
     */
    Class<?> value();

}
