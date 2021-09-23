package bh.bot.common.types.annotations;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AppMeta {
    String code();
    String name();
    boolean dev() default false;
    double displayOrder() default Double.MAX_VALUE;
    String argType() default "";
    String argDefault() default "";
}