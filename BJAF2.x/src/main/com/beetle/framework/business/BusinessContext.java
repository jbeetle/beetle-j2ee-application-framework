package com.beetle.framework.business;

import com.beetle.framework.resource.dic.DIContainer;

public final class BusinessContext {

	private BusinessContext() {
		super();
	}

	/**
	 * 从本地获取Service实例（服务层定义的事务属性和同步属性有效）
	 * 
	 * @param face
	 *            服务的接口类
	 * @return Service实例
	 */
	final public static <T> T serviceLookup(Class<T> face) {
		// return ServiceFactory.localServiceLookup(face);
		return DIContainer.getInstance().retrieve(face);
	}

	final public static Object serviceLookup(String key) {
		// return ServiceFactory.localServiceLookup(face);
		return DIContainer.getInstance().retrieve(key);
	}
}
