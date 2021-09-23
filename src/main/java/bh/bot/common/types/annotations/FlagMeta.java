package bh.bot.common.types.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FlagMeta {
    String cbDesc() default "";
    String cbVal() default "";
    boolean hide() default false;
    boolean checked() default false;
    double displayOrder() default 100;
}
