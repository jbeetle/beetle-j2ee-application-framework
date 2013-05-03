package com.beetle.framework.resource.dic;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.beetle.framework.AppRuntimeException;
import com.beetle.framework.log.AppLogger;
import com.beetle.framework.resource.dic.aop.Aop;
import com.beetle.framework.resource.dic.aop.AopInterceptor;
import com.beetle.framework.resource.dic.aop.AopInterceptor.InnerHandler;

/**
 * 关系依赖管理
 * 
 * @author HenryYu
 * 
 */
public class ReleBinder {
	private final List<BeanVO> beanVoList;
	private static AppLogger logger = AppLogger.getInstance(ReleBinder.class);
	private final static Map<String, Object> DI_AOP_PROXY_CACHE = new ConcurrentHashMap<String, Object>();

	public ReleBinder() {
		super();
		this.beanVoList = new ArrayList<BeanVO>();
	}

	public static Object getProxyFromCache(String key) {
		return DI_AOP_PROXY_CACHE.get(key);
	}

	public List<BeanVO> getBeanVoList() {
		return beanVoList;
	}

	public static class FieldVO {

		private String name;

		private String ref;

		public FieldVO(String name, String ref) {
			this.name = name;
			this.ref = ref;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getRef() {
			return ref;
		}

		public void setRef(String ref) {
			this.ref = ref;
		}

		@Override
		public String toString() {
			return "PropertyVO [name=" + name + ", ref=" + ref + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((ref == null) ? 0 : ref.hashCode());
			return result;
		}

	}

	public static class BeanVO {
		public BeanVO(Class<?> iface, Class<?> imp, boolean single, int flag) {
			super();
			this.iface = iface;
			this.imp = imp;
			this.single = single;
			this.flag = flag;
			this.properties = new ArrayList<FieldVO>();
		}

		public BeanVO(String aopId, AopInterceptor interceptor) {
			super();
			this.aopId = aopId;
			this.interceptor = interceptor;
			this.properties = new ArrayList<FieldVO>();
		}

		private String aopId;
		private AopInterceptor interceptor;
		private Method aopMethod;
		private Class<?> iface;
		private Class<?> imp;
		private boolean single;
		private int flag;
		private final List<FieldVO> properties;// for inject

		public Method getAopMethod() {
			return aopMethod;
		}

		public void setAopMethod(Method aopMethod) {
			this.aopMethod = aopMethod;
		}

		public List<FieldVO> getProperties() {
			return properties;
		}

		public String getAopId() {
			return aopId;
		}

		public AopInterceptor getInterceptor() {
			return interceptor;
		}

		@SuppressWarnings("rawtypes")
		public Class getIface() {
			return iface;
		}

		public Class<?> getImp() {
			return imp;
		}

		public int getFlag() {
			return flag;
		}

		public boolean isSingle() {
			return single;
		}

		@Override
		public String toString() {
			return "BeanVO [aopId=" + aopId + ", interceptor=" + interceptor
					+ ", aopMethod=" + aopMethod + ", iface=" + iface
					+ ", imp=" + imp + ", single=" + single + ", flag=" + flag
					+ ", properties=" + properties + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((aopId == null) ? 0 : aopId.hashCode());
			result = prime * result
					+ ((aopMethod == null) ? 0 : aopMethod.hashCode());
			result = prime * result + flag;
			result = prime * result + ((iface == null) ? 0 : iface.hashCode());
			result = prime * result + ((imp == null) ? 0 : imp.hashCode());
			result = prime * result
					+ ((interceptor == null) ? 0 : interceptor.hashCode());
			result = prime * result
					+ ((properties == null) ? 0 : properties.hashCode());
			result = prime * result + (single ? 1231 : 1237);
			return result;
		}

	}

	protected void bindAopInterceptor(String aopid, AopInterceptor interceptor) {
		beanVoList.add(new BeanVO(aopid, interceptor));
	}

	/**
	 * 自我绑定
	 * 
	 * @param clazz
	 *            实现类
	 * @param singleton
	 *            是否单例（true--单例）
	 */
	void bind(Class<?> clazz, boolean singleton) {
		beanVoList.add(new BeanVO(null, clazz, singleton, 1));
	}

	/**
	 * 接口-实现（包括：抽象--扩展）绑定
	 * 
	 * @param interfaceClazz
	 *            接口类（或抽象类）
	 * @param implementClazz
	 *            实现类（扩展类）
	 * @param singleton
	 */
	void bind(Class<?> interfaceClazz, Class<?> implementClazz,
			boolean singleton) {
		if (!interfaceClazz.isAssignableFrom(implementClazz)) {
			throw new AppRuntimeException("err:"
					+ interfaceClazz.getSimpleName() + " must be "
					+ implementClazz.getSimpleName()
					+ "'s father or interface!");
		}
		beanVoList
				.add(new BeanVO(interfaceClazz, implementClazz, singleton, 2));
	}

	/**
	 * 从配置进行绑定
	 * 
	 * @param xmlFileInputStream
	 *            --配置文件输入流
	 */
	public void bindFromConfig(InputStream xmlFileInputStream) {
		SAXReader reader = new SAXReader();
		Document doc = null;
		try {
			doc = reader.read(xmlFileInputStream);
			gendoc(doc);
			// bindProperties();
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

	/**
	 * 从配置进行绑定 文件为XML，格式如下：<br>
	 * 
	 * <pre>
	 *  <binder>
	 *     <!-- 
	 * interface-服务接口或抽象类，没有接口可不定义此属性
	 * implement-接口实现类，抽象的扩展类，或提供者类，或非接口实现类（此时不定义interface属性）
	 * singleton-是否为单例（针对implement而已），不定义此属性，默认为true
	 * 	-->
	 * 	<item interface="com.beetle.framework.util.pattern.di.IService" implement="com.beetle.framework.util.pattern.di.IServiceImp" singleton="true"/>
	 * </binder>
	 * </pre>
	 * 
	 * <br>
	 * 
	 * @param f
	 *            --指定配置文件
	 */
	public void bindFromConfig(File f) {
		SAXReader reader = new SAXReader();
		Document doc = null;
		try {
			doc = reader.read(f);
			gendoc(doc);
			// bindProperties();
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

	private void gendoc(Document doc) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Node node = doc.selectSingleNode("binder");
		if (node != null) {
			Iterator<?> it = node.selectNodes("item").iterator();
			while (it.hasNext()) {
				Element e = (Element) it.next();
				String aopid = e.valueOf("@aopid");
				if (aopid != null && aopid.trim().length() > 0) {
					String inerceptor = e.valueOf("@interceptor");
					this.bindAopInterceptor(aopid, (AopInterceptor) Class
							.forName(inerceptor.trim()).newInstance());
				} else {
					dealInject(e);
				}
			}
		}
	}

	private void dealInject(Element e) throws ClassNotFoundException {
		String face = e.valueOf("@interface");
		String imp = e.valueOf("@implement");
		//
		// 绑定以便后面校验
		// AppExchanger.getInstance().bind(face, imp);
		//
		String singleton = e.valueOf("@singleton");
		boolean sf = false;
		if (singleton == null || singleton.trim().length() == 0) {
			sf = true;
		} else {
			if (singleton.trim().equalsIgnoreCase("true")) {
				sf = true;
			}
		}
		if (imp == null || imp.trim().length() == 0) {
			throw new DependencyInjectionException(
					"config content formate err,implement can't be null");
		}
		if (face == null || face.trim().length() == 0) {
			bind(Class.forName(imp.trim()), sf);
		} else {
			if (Class.forName(face.trim()).isAssignableFrom(
					Class.forName(imp.trim()))) {
				bind(Class.forName(face.trim()), Class.forName(imp.trim()), sf);
			} else {
				throw new DependencyInjectionException("not support this case!");
			}
		}
	}

	public void bindProperties() {
		logger.debug("beanVoList size:{}", beanVoList.size());
		logger.debug("beanVoList:{}", beanVoList);
		for (BeanVO vo : beanVoList) {
			setBeanVoProperty(vo);
		}
	}

	private void setBeanVoProperty(BeanVO bvo) {
		logger.debug("bvo:{}", bvo);
		Class<?> imp = bvo.getImp();
		if (imp == null) {// aop case
			return;
		}
		Field[] fields = imp.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			for (Field f : fields) {
				if (f.isAnnotationPresent(Inject.class)) {
					FieldVO pvo = new FieldVO(f.getName(), f.getType()
							.getName());
					bvo.getProperties().add(pvo);
					logger.debug("pvo:{}", pvo);
				}
			}
		}
		Method[] methods = imp.getDeclaredMethods();
		for (Method m : methods) {
			if (m.isAnnotationPresent(Aop.class)) {
				String aopId_m = m.getAnnotation(Aop.class).id();
				if (aopId_m == null || aopId_m.length() == 0) {
					throw new DependencyInjectionException(
							logger.strFormat(
									"Initialization error of AOP,the method[{}]'s aop id must be setted!",
									m));
				}
				//TODO
				if (bvo.getAopId().equals(aopId_m)) {
					bvo.setAopMethod(m);
				}
				AopInterceptor interceptor = bvo.getInterceptor();
				if (interceptor != null && bvo.getIface() != null) {
					try {
						if (!DI_AOP_PROXY_CACHE.containsKey(bvo.getIface()
								.getName())) {
							Object proxy = Proxy.newProxyInstance(bvo
									.getIface().getClassLoader(),
									new Class<?>[] { bvo.getIface() },
									new InnerHandler(bvo.getIface()));
							DI_AOP_PROXY_CACHE.put(bvo.getIface().getName(),
									proxy);
							logger.debug("bind proxy:{},bvo:{}", proxy, bvo);
						}
					} catch (Exception e) {
						throw new DependencyInjectionException(
								"Initialization error of AOP", e);
					}
				}
			}
		}

	}
}
