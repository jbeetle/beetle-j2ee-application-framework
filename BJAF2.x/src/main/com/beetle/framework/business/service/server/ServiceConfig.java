package com.beetle.framework.business.service.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.beetle.framework.AppProperties;
import com.beetle.framework.AppRuntimeException;
import com.beetle.framework.business.BusinessContext;
import com.beetle.framework.business.common.tst.ServiceMethodWithSynchronized;
import com.beetle.framework.business.common.tst.ServiceMethodWithTransaction;
import com.beetle.framework.business.common.tst.aop.ServiceTransactionRegister;
import com.beetle.framework.log.AppLogger;
import com.beetle.framework.util.ClassUtil;
import com.beetle.framework.util.ResourceLoader;

public class ServiceConfig {
	private final static Map<String, ServiceDef> scache = new ConcurrentHashMap<String, ServiceConfig.ServiceDef>();
	private final static AppLogger logger = AppLogger
			.getInstance(ServiceConfig.class);

	static {
		String filename = AppProperties.getAppHome() + "ServiceConfig.xml";
		File f = new File(filename);
		if (f.exists()) {
			loadFromConfig(f);
			logger.info("load services from file[{}]", filename);
		} else {
			try {
				loadFromConfig(ResourceLoader.getResAsStream(filename));
				logger.info("load services from resourceloader[{}]", filename);
			} catch (IOException e) {
				logger.warn("no [{}] file found,not load service define data",
						filename);
			}
		}
		// ServiceTransactionRegister.register();
		// command组件内置rpc服务
		ServiceDef cmd = new ServiceDef();
		cmd.setEnabled("true");
		cmd.setIface("com.beetle.framework.business.command.imp.rpc.ICmdService");
		cmd.setImp("com.beetle.framework.business.command.imp.rpc.ServiceCmdImp");
		register(cmd);
		//
	}

	private static void loadFromConfig(InputStream xmlFileInputStream) {
		SAXReader reader = new SAXReader();
		Document doc = null;
		try {
			doc = reader.read(xmlFileInputStream);
			gendoc(doc);
		} catch (Exception de) {
			throw new AppRuntimeException(de);
		} finally {
			if (doc != null) {
				doc.clearContent();
				doc = null;
			}
			reader = null;
		}
	}

	private static void loadFromConfig(File f) {
		SAXReader reader = new SAXReader();
		Document doc = null;
		try {
			doc = reader.read(f);
			gendoc(doc);
		} catch (Exception de) {
			throw new AppRuntimeException(de);
		} finally {
			if (doc != null) {
				doc.clearContent();
				doc = null;
			}
			reader = null;
		}
	}

	private static void gendoc(Document doc) throws ClassNotFoundException {
		Node node = doc.selectSingleNode("binder");
		if (node != null) {
			Iterator<?> it = node.selectNodes("item").iterator();
			while (it.hasNext()) {
				ServiceDef sdf = new ServiceDef();
				Element e = (Element) it.next();
				String face = e.valueOf("@interface");
				String imp = e.valueOf("@implement");
				String enabled = e.valueOf("@enabled");
				sdf.setIface(face);
				sdf.setImp(imp);
				sdf.setEnabled(enabled);
				@SuppressWarnings("unchecked")
				Iterator<Attribute> ait = e.attributeIterator();
				while (ait.hasNext()) {
					Attribute at = ait.next();
					sdf.addExtension(at.getName(), at.getValue());
				}
				register(sdf);
			}
		}
	}

	public static ServiceDef lookup(String iface) {
		return scache.get(iface);
	}

	public static void register(ServiceDef sdf) {
		scache.put(sdf.getIface(), sdf);
	}

	private final static Map<String, Object> serviceInstanceCache = new ConcurrentHashMap<String, Object>();

	public static class ServiceDef {
		public static class MethodEx {
			private Method method;
			private boolean withTransaction;
			private boolean withSynchronized;

			public Method getMethod() {
				return method;
			}

			public void setMethod(Method method) {
				this.method = method;
			}

			public boolean isWithTransaction() {
				return withTransaction;
			}

			public boolean isWithSynchronized() {
				return withSynchronized;
			}

			public void setWithSynchronized(boolean withSynchronized) {
				this.withSynchronized = withSynchronized;
			}

			public void setWithTransaction(boolean withTransaction) {
				this.withTransaction = withTransaction;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((method == null) ? 0 : method.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				MethodEx other = (MethodEx) obj;
				if (method == null) {
					if (other.method != null)
						return false;
				} else if (!method.equals(other.method))
					return false;
				return true;
			}

		}

		public MethodEx getMethodEx(String methodName, Class<?>[] parameterTypes)
				throws Exception {
			final String key;
			StringBuilder sb = new StringBuilder();
			// sb.append(methodName);
			for (Class<?> ca : parameterTypes) {
				sb.append(ca.getName());
			}
			key = methodName + "_" + parameterTypes.length + "_"
					+ sb.toString().length();// 参数个数相同，而且参数类型都一样，但顺序不同的情况搞不定
			MethodEx m = this.methodCache.get(key);
			if (m != null) {
				return m;
			}
			synchronized (this) {
				if (!methodCache.containsKey(key)) {
					Object impl = this.getServiceImpInstanceRef();
					Method tm = impl.getClass().getDeclaredMethod(methodName,
							parameterTypes);
					MethodEx mex = new MethodEx();
					mex.setMethod(tm);
					mex.setWithTransaction(tm
							.isAnnotationPresent(ServiceMethodWithTransaction.class));
					mex.setWithSynchronized(tm
							.isAnnotationPresent(ServiceMethodWithSynchronized.class));
					methodCache.put(key, mex);
				}
				return methodCache.get(key);
			}
		}

		public Object getServiceImpInstanceAop() {
			if (serviceInstanceCache.containsKey(this.iface)) {
				return serviceInstanceCache.get(this.iface);
			}
			synchronized (this) {
				Object o = serviceInstanceCache.get(iface);
				if (o == null) {
					try {
						Class<?> c = Class.forName(this.imp);
						o = ServiceTransactionRegister.retrieveService(c);
						if (ClassUtil.isThreadSafe(c)) {
							serviceInstanceCache.put(iface, o);
						}
					} catch (Exception e) {
						throw new AppRuntimeException(e);
					}
				}
				return o;
			}
		}

		public Object getServiceImpInstanceRef() {
			try {
				return BusinessContext.getInstance().retrieveFromDiContainer(
						Class.forName(this.iface));
			} catch (ClassNotFoundException e) {
				logger.error(e);
				return null;
			}
		}

		Object getServiceImpInstanceRef_bak() {
			if (serviceInstanceCache.containsKey(this.iface)) {
				return serviceInstanceCache.get(this.iface);
			}
			synchronized (this) {
				Object o = serviceInstanceCache.get(iface);
				if (o == null) {
					try {
						@SuppressWarnings("rawtypes")
						Class c = Class.forName(this.imp);
						o = c.newInstance();
						if (ClassUtil.isThreadSafe(c)) {
							serviceInstanceCache.put(iface, o);
						}
					} catch (Exception e) {
						logger.error(e);
						return null;
						// throw new AppRuntimeException(e);
					}
				}
				return o;
			}
		}

		public ServiceDef() {
			this.extensions = new HashMap<String, String>();
			this.methodCache = new HashMap<String, MethodEx>();
		}

		private String iface;
		private String imp;
		private String enabled;
		private final Map<String, MethodEx> methodCache;

		public String getEnabled() {
			return enabled;
		}

		public void setEnabled(String enabled) {
			this.enabled = enabled;
		}

		private Map<String, String> extensions;

		public String getIface() {
			return iface;
		}

		public void setIface(String iface) {
			this.iface = iface;
		}

		public String getImp() {
			return imp;
		}

		public void setImp(String imp) {
			this.imp = imp;
		}

		public Map<String, String> getExtensions() {
			return extensions;
		}

		public void addExtension(String key, String value) {
			extensions.put(key, value);
		}

		public String getExtensionValue(String key) {
			return extensions.get(key);
		}

		@Override
		public String toString() {
			return "ServiceDef [" + extensions + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((iface == null) ? 0 : iface.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ServiceDef other = (ServiceDef) obj;
			if (iface == null) {
				if (other.iface != null)
					return false;
			} else if (!iface.equals(other.iface))
				return false;
			return true;
		}
	}
}
