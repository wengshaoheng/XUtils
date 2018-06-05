package wsh.io.cfgutil;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XConfKey {
	/**
	 * 
	 * @return
	 */
	String value() default "";
	
	/**
	 * 
	 * @return
	 */
	String namespace() default "";
}
