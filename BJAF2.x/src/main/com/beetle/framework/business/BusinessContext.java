package com.beetle.framework.business;

import com.beetle.framework.business.service.ServiceFactory;
import com.beetle.framework.resource.dic.DIContainer;

public final class BusinessContext {
	private static BusinessContext instance = new BusinessContext();

	private BusinessContext() {
		super();
	}

	public static BusinessContext getInstance() {
		return instance;
	}

	/**
	 * 从DI容量里面获取实例（注意：通过此方式获取的实例服务层定义的事务属性和同步属性无效）
	 * 
	 * @param faceOrImpClass
	 * @return
	 */
	public <T> T retrieveFromDiContainer(Class<T> faceOrImpClass) {
		return DIContainer.getInstance().retrieve(faceOrImpClass);
	}

	/**
	 * 从本地获取Service实例（服务层定义的事务属性和同步属性有效）
	 * 
	 * @param face
	 *            服务的接口类
	 * @return Service实例
	 */
	final public static <T> T serviceLookup(Class<T> face) {
		return ServiceFactory.localServiceLookup(face);
	}
}
