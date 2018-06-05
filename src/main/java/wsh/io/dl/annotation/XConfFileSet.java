package wsh.io.dl.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Shaoheng.Weng (Mars)
 */
public @Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface XConfFileSet {
	XConfFile[] value() default {};
}
