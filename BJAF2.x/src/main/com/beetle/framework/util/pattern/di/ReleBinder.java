package com.beetle.framework.util.pattern.di;

import com.beetle.framework.AppRuntimeException;
import com.beetle.framework.util.pattern.aop.AopInterceptor;
import com.beetle.framework.util.pattern.aop.AopNames;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.matcher.Matchers;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 关系依赖管理
 * 
 * @author HenryYu
 * 
 */
public class ReleBinder {
	private final List<BindVO> dlist;
	private TT moduler;

	TT getModuler() {
		return moduler;
	}

	private class TT extends AbstractModule {
		public TT() {
			super();
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void configure() {
			try {
				for (BindVO vo : dlist) {
					if (vo.getAopId() != null && vo.getInterceptor() != null) {
						this.bindInterceptor(Matchers.any(), Matchers
								.annotatedWith(AopNames.named(vo.getAopId())),
								vo.getInterceptor());
						continue;
					}
					if (vo.getFlag() == 1) {
						this.bind(vo.getImp()).in(
								(vo.isSingle()) ? Scopes.SINGLETON
										: Scopes.NO_SCOPE);
					} else if (vo.getFlag() == 2) {
						this.bind(vo.getIface())
								.to(vo.getImp())
								.in((vo.isSingle()) ? Scopes.SINGLETON
										: Scopes.NO_SCOPE);
					} else if (vo.getFlag() == 3) {
						this.bind(vo.getIface()).toProvider(vo.getImp())
								.in(Scopes.SINGLETON);
					} else {
						throw new AppRuntimeException("not support yet!");
					}
				}
			} finally {
				dlist.clear();
			}
		}
	}

	public ReleBinder() {
		super();
		this.dlist = new ArrayList<BindVO>();
		this.moduler = new TT();
	}

	private static class BindVO {
		public BindVO(Class<?> iface, Class<?> imp, boolean single, int flag) {
			super();
			this.iface = iface;
			this.imp = imp;
			this.single = single;
			this.flag = flag;
		}

		public BindVO(String aopId, AopInterceptor interceptor) {
			super();
			this.aopId = aopId;
			this.interceptor = interceptor;
		}

		private String aopId;
		private AopInterceptor interceptor;

		private Class<?> iface;
		private Class<?> imp;
		private boolean single;
		private int flag;

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
	}

	protected void bindAopInterceptor(String aopid, AopInterceptor interceptor) {
		dlist.add(new BindVO(aopid, interceptor));
	}

	/**
	 * 自我绑定
	 * 
	 * @param clazz
	 *            实现类
	 * @param singleton
	 *            是否单例（true--单例）
	 */
	public void bind(Class<?> clazz, boolean singleton) {
		dlist.add(new BindVO(null, clazz, singleton, 1));
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
	public void bind(Class<?> interfaceClazz, Class<?> implementClazz,
			boolean singleton) {
		if (!interfaceClazz.isAssignableFrom(implementClazz)) {
			throw new AppRuntimeException("err:"
					+ interfaceClazz.getSimpleName() + " must be "
					+ implementClazz.getSimpleName()
					+ "'s father or interface!");
		}
		dlist.add(new BindVO(interfaceClazz, implementClazz, singleton, 2));
	}

	/**
	 * 服务--提供者（可灵活运行时根据逻辑选定实现）绑定
	 * 
	 * @param service
	 *            服务接口（抽象）类
	 * @param serviceProvider
	 *            服务提供者类（必须是ServiceProvider的实现）
	 */
	public void bind(Class<?> service, Class<?> serviceProvider) {
		if (!ServiceProvider.class.isAssignableFrom(serviceProvider)) {
			throw new AppRuntimeException("err:"
					+ serviceProvider.getSimpleName() + " must extend "
					+ ServiceProvider.class.getName());
		}
		dlist.add(new BindVO(service, serviceProvider, false, 3));
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
			throw new AppRuntimeException(
					"config content formate err,implement can't be null");
		}
		if (face == null || face.trim().length() == 0) {
			bind(Class.forName(imp.trim()), sf);
		} else {
			if (Class.forName(face.trim()).isAssignableFrom(
					Class.forName(imp.trim()))) {
				bind(Class.forName(face.trim()), Class.forName(imp.trim()), sf);
			} else {
				bind(Class.forName(face.trim()), Class.forName(imp.trim()));
			}
		}
	}
}
