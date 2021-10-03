package bh.bot.common.types.annotations;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AppMeta {
    String code();
    String name();
    boolean requireClientType() default true;
    boolean dev() default false;
    double displayOrder() default Double.MAX_VALUE;
    String argType() default "";
    String argAsk() default "";
    String argDefault() default "";
    boolean argRequired() default false;
}