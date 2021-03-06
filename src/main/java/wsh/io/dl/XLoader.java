package wsh.io.dl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 
 * @author Shaoheng.Weng (Mars)
 */
public abstract class XLoader {
	
	private static final Logger LOG = Logger.getLogger(XLoader.class.getName());
	
	/**
	 * resolve the specific annotation annotated the given bean
	 * @param annoClz - annotation annotated the given bean
	 * @param beanClz - bean annotated by the given annotation
	 * @return
	 */
	protected <T extends Annotation> List<T> resolveClassAnnotation (
			Class<T> annoClz, Class<?> beanClz) {
		
		return resolveClassAnnotation(null, annoClz, beanClz);
	}
	
	/**
	 * resolve the setAnnotation or elementAnnotation annotated the given bean
	 * @param setAnnotation set annotation which contains elementAnnotation elements
	 * @param elementAnnotation element annotation
	 * @param beanClz bean annotated by setAnnotation or elementAnnotation
	 * @return List with element of T type
	 */
	protected <S extends Annotation, E extends Annotation> List<E> resolveClassAnnotation (
			Class<S> setAnnotation, Class<E> elementAnnotation, Class<?> beanClz) {
		
		List<E> eList = new ArrayList<E>();
		try {
			if (beanClz.isAnnotationPresent(elementAnnotation)) {
				LOG.info(beanClz + " is annotated by " + elementAnnotation);
				eList.addAll(Arrays.asList(beanClz.getAnnotationsByType(elementAnnotation)));
			}
			
			if (setAnnotation != null && beanClz.isAnnotationPresent(setAnnotation)) {
				LOG.info(beanClz + " is annotated by " + setAnnotation);
				S set = beanClz.getAnnotation(setAnnotation);
				Method valueMethod = setAnnotation.getMethod("value");
				@SuppressWarnings("unchecked")
				E[] ea = (E[])valueMethod.invoke(set);
				if (ea.length > 0) {
					LOG.info(setAnnotation + " has " + ea.length + 
							" " + elementAnnotation + " elements");
					eList.addAll(Arrays.asList(ea));
				}
			}
		} catch (Exception e) {
			LOG.severe("Failed to resolve class annotation:" + e.getMessage());
			throw new RuntimeException("Failed to resolve class annotation", e);
		}
		
		if (eList.isEmpty()) {
			String msg = "No " + elementAnnotation + " annotated on " + beanClz + " found";
			LOG.severe(msg);
			throw new RuntimeException(msg);
		}
		
		return eList;
	}
	
	/**
	 * resolve the specified annotation annotated 
	 * @param annoClz
	 * @param beanClz
	 * @return List with element 
	 */
	protected <T extends Annotation> Map<String, T> resolveFieldAnnotation(
			Class<T> annoClz, Class<?> beanClz) {
		
		Map<String, T> aMap = new HashMap<String, T>();
		
		Field[] fields = beanClz.getDeclaredFields();
		
		for (Field field : fields) {
			if (field.isAnnotationPresent(annoClz)) {
				aMap.put(field.getName(), field.getDeclaredAnnotation(annoClz));
			}
		}
		
		return aMap;
	}
	
	/**
	 * resolve the file source to an byte array input stream
	 * @param filePath - the input file 
	 */
	protected InputStream resolveInputStream(String filePath) {
		
		InputStream is = null;
		
		try {
			if (filePath.startsWith("classpath:")) {
				String path = filePath.substring(10);
				is = this.getClass().getResourceAsStream(path);
			} else if (filePath.startsWith("file://")) {
				String path = filePath.substring(7);
				is = new FileInputStream(path);
			} else {
				is = new FileInputStream(filePath);
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int data = -1;
			while ( (data = is.read()) != -1) {
				baos.write(data);
			}
			return new ByteArrayInputStream(baos.toByteArray());
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {if (is != null) is.close();} catch (Exception e) {}
		}
	}
	
	/**
	 * resolve the setter method name for the given field name
	 */
	protected String resolveSetterName(String fieldName) {
		return "set" + String.valueOf(fieldName.charAt(0)).toUpperCase() + 
				fieldName.substring(1);
	}
	
	/**
	 * resolve the given field's value for given the bean object
	 * @param beanObj
	 * @param fieldName
	 * @param value
	 */
	protected void resolveFieldValue(Object beanObj, String fieldName, String value) {
		String setterName = resolveSetterName(fieldName);
		LOG.info("resolved setter name -> " + setterName + " for " + beanObj.getClass());
		try {
			Field field   = beanObj.getClass().getDeclaredField(fieldName);
			LOG.info("Field " + fieldName + " -> " + field.getType());
			Method setter = beanObj.getClass().getDeclaredMethod(setterName, field.getType());
			Object v = resolveToJavaMappingValue(field.getType(), value);
			setter.invoke(beanObj, v);
		} catch (Exception e) {
			LOG.severe("Failed to resolve the value for field ->" + fieldName);
			throw new RuntimeException("Failed to resolve field value", e);
		}
	}
	
	/**
	 * resolve the field value with the given type
	 * @param objClz - the value's target type
	 * @param rawValue  -  value hasn't been handled
	 */
	protected abstract Object resolveToJavaMappingValue(Class<?> objClz, Object rawValue);
	
}
