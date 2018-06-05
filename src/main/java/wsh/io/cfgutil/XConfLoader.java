package wsh.io.cfgutil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

public class XConfLoader extends XLoader {
	
	private static final Logger LOG = Logger.getLogger(XConfLoader.class.getName());
	
	private static final Map<String, Properties> _cfgStore = 
			new HashMap<String, Properties>();
	
	private void storeConfig(String namespace, Properties prop) {
		_cfgStore.put(namespace, prop);
	}
	
	/**
	 * find out the given name space configuration object, if not found, 
	 * will return a properties object with merged all name spaces properties
	 * @param namespace
	 */
	private Properties fetchConfig(String namespace) {
		Properties prop = null;
		if (namespace != null && !"".equals(namespace.trim())) {
			prop = _cfgStore.get(namespace);
		}
		
		if (prop == null) {
			prop = new Properties();
			Set<String> keySet = _cfgStore.keySet();
			for (String key : keySet) {
				prop.putAll(_cfgStore.get(key));
			}
		}
		
		return prop;
	}
	
	/**
	 * register a given class annotated with XConfFile
	 * @param cfgClz - class annotated by annotation XConfFile or XConfFileSet 
	 */
	public void register(Class<?> ...cfgClzs ) {
		if (cfgClzs != null && cfgClzs.length > 0) {
			for (Class<?> cfgClz : cfgClzs) {
				List<XConfFile> cfList = this.resolveClassAnnotation(XConfFileSet.class, XConfFile.class, cfgClz);
				int index = 1;
				for (XConfFile cf : cfList) {
					String cfgFilePath = cf.value();
					LOG.info("Start to parse " + cfgFilePath);
					try {
						InputStream is = resolveInputStream(cfgFilePath);
						Properties prop = new Properties();
						prop.load(is);
						storeConfig(String.valueOf(index++), prop);
					} catch (Exception e) {
						LOG.severe("Failed to parse " + cfgFilePath + " :" + e.getMessage());
						throw new RuntimeException("Failed to register " + cfgClz, e);
					}
				}
			}
			LOG.info(_cfgStore.toString());	
		}
	}
	
	/**
	 * Load a bean for given bean class and name space
	 * @param beanClz   - bean class
	 * @param namespace - properties name space, can be null
	 */
	public <T> T loadConfigBean(Class<T> beanClz, String namespace)  {
		try {
			T z = beanClz.newInstance();
			Properties prop = this.fetchConfig(namespace);
			Map<String, XConfKey> aMap = resolveFieldAnnotation(XConfKey.class, beanClz);
			Set<String> keySet = aMap.keySet();
			for (String key : keySet) {
				String cfgKey = aMap.get(key).value();
				resolveFieldValue(z, key, prop.getProperty(cfgKey));
			}
			return z;
		} catch (Exception e) {
			String msg = "Failed to load configuration bean " + beanClz + " : " + e.getMessage();
			LOG.severe(msg);
			throw new RuntimeException(msg);
		}
	}
	
	/**
	 * Load Configuration Bean by given bean class
	 */
	public <T> T loadConfigBean(Class<T> beanClz) {
		return loadConfigBean(beanClz, null);
	}

}
