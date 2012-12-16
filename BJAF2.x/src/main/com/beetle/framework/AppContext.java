/*
 * BJAF - Beetle J2EE Application Framework
 * 甲壳虫J2EE企业应用开发框架
 * 版权所有2003-2015 余浩东 (www.beetlesoft.net)
 * 
 * 这是一个免费开源的软件，您必须在
 *<http://www.apache.org/licenses/LICENSE-2.0>
 *协议下合法使用、修改或重新发布。
 *
 * 感谢您使用、推广本框架，若有建议或问题，欢迎您和我联系。
 * 邮件： <yuhaodong@gmail.com/>.
 */
package com.beetle.framework;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.beetle.framework.log.AppLogger;
import com.beetle.framework.util.ResourceLoader;
import com.beetle.framework.util.pattern.di.DIContainer;
import com.beetle.framework.util.pattern.di.ReleBinder;

public class AppContext {
	private final static ConcurrentHashMap<String, Object> table = new ConcurrentHashMap<String, Object>();
	private static AppContext instance = new AppContext();
	static final String appHomePath = "beetle.application.home.path";
	private DIContainer di;

	public void setAppHomePath(String homePath) {
		bind(appHomePath, homePath);
	}

	private void initGlobalDi() {
		synchronized (this) {
			if (di == null) {
				ReleBinder rb = new ReleBinder();
				loadXmlFile(rb, "DAOConfig.xml");
				loadXmlFile(rb, "ServiceConfig.xml");
				loadXmlFile(rb, "AOPConfig.xml");
				di = new DIContainer(rb);
			}
		}
	}

	public <T> T lookup(Class<T> faceOrImpClass) {
		if (di == null) {
			initGlobalDi();
		}
		return di.retrieve(faceOrImpClass);
	}

	private void loadXmlFile(ReleBinder rb, String xmlname) {
		String filename = AppProperties.getAppHome() + xmlname;
		File f = new File(filename);
		if (f.exists()) {
			rb.bindFromConfig(f);
			AppLogger.getInstance(AppContext.class).info("loaded {} from file",
					filename);
		} else {
			InputStream is = null;
			try {
				is = ResourceLoader.getResAsStream(filename);
				rb.bindFromConfig(is);
				AppLogger.getInstance(AppContext.class).info(
						"loaded {} from resource", filename);
			} catch (IOException e) {
				AppLogger.getInstance(AppContext.class).warn(e.getMessage());
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	public String getAppHomePathDefineFromContext() {
		return (String) lookup(appHomePath);
	}

	String getAppHome() {
		String fp = System.getProperty(appHomePath);
		if (fp != null && fp.trim().length() > 0) {
			if (!fp.endsWith("/")) {
				fp = fp + "/";
			}
			return fp;
		} else {
			String ap = (String) AppContext.getInstance().lookup(appHomePath);
			if (ap != null && ap.trim().length() > 0) {
				if (!ap.endsWith("/")) {
					ap = ap + "/";
				}
				return ap;
			}
		}
		return "config/";
	}

	public Enumeration<String> getContextKeys() {
		return table.keys();
	}

	private AppContext() {
		// table = new Hashtable();
	}

	public void bind(String name, Object obj) {
		table.put(name, obj);
	}

	public void close() {
		if (!table.isEmpty()) {
			table.clear();
		}
	}

	public Map<String, Object> getEnvironment() {
		return table;
	}

	public Object lookup(String name) {
		return table.get(name);
	}

	public void rebind(String name, Object obj) {
		if (table.containsKey(name)) {
			table.remove(name);
		}
		table.put(name, obj);
	}

	public void unbind(String name) {
		if (table.containsKey(name)) {
			table.remove(name);
		}
	}

	public static AppContext getInstance() {
		return instance;
	}
}
