package io.github.taybct.tool.scheduledlogcentralized.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManagedScheduled {
    String name() default "";
}