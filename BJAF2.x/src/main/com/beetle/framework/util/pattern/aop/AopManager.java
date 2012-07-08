package com.beetle.framework.util.pattern.aop;

import com.beetle.framework.util.pattern.di.DIContainer;
import com.beetle.framework.util.pattern.di.ReleBinder;

public class AopManager {
	private static class RB extends ReleBinder {

		@Override
		protected void bindAopInterceptor(String aopid,
				AopInterceptor interceptor) {
			super.bindAopInterceptor(aopid, interceptor);
		}
	}

	/**
	 * 创建一个新的管理员实例
	 * 
	 * @return
	 */
	public static AopManager createNewManager() {
		return new AopManager();
	}

	/**
	 * 获取一个全局的管理实例（单例）
	 * 
	 * @return
	 */
	public static AopManager getGlobalManager() {
		if (instance == null) {
			instance = new AopManager();
		}
		return instance;
	}

	private RB rb;
	private volatile DIContainer di;
	private static volatile AopManager instance;

	private AopManager() {
		rb = new RB();
	}

	/**
	 * 绑定拦截器与被拦截的声明的注解（相当于绑定拦截器与其对应的方法）
	 * 
	 * @param aopid
	 *            --方法上Aop注解的标记，如：（@Aop(id = "xxx")）
	 * @param interceptor
	 *            --拦截器的实现实例
	 */
	public void bind(String aopid, AopInterceptor interceptor) {
		rb.bindAopInterceptor(aopid, interceptor);
	}

	/**
	 * 根据被拦截的服务类，从管理器中获取其对应的实例
	 * 
	 * @param <T>
	 * @param serviceAoped
	 * @return
	 */
	public <T> T retrieve(Class<T> serviceAoped) {
		if (di == null) {
			di = new DIContainer(rb);
		}
		return di.retrieve(serviceAoped);
	}
}
