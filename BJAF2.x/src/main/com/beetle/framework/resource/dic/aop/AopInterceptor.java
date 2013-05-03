package com.beetle.framework.resource.dic.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.beetle.framework.resource.dic.DIContainer;
import com.beetle.framework.resource.dic.ReleBinder;
import com.beetle.framework.resource.dic.ReleBinder.BeanVO;

/**
 * AOP方法拦截器 实现拦截方法前后执行功能，<br>
 * 如果before/after不能满足要求，可以重载invoke方法来进行更为灵活的拦截操作。
 */
public abstract class AopInterceptor {
	public static class InnerHandler implements InvocationHandler {
		private final Class<?> targetImpFace;
		private static final Map<Method, AopInterceptor> CACHE = new ConcurrentHashMap<Method, AopInterceptor>();

		public InnerHandler(Class<?> targetImpFace) {
			super();
			this.targetImpFace = targetImpFace;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			Object rs = null;
			AopInterceptor interceptor = getInterceptor(method);
			if (interceptor != null) {
				interceptor.before(method.getName(), args);
			}
			Object targetImp = DIContainer.Inner
					.getBeanFromDIBeanCache(targetImpFace.getName());
			rs = method.invoke(targetImp, args);
			if (interceptor != null) {
				interceptor.after(rs, method.getName(), args);
			}
			return rs;
		}

		private AopInterceptor getInterceptor(Method method) {
			AopInterceptor interceptor = CACHE.get(method);
			if (interceptor == null) {
				synchronized (CACHE) {
					if (interceptor == null) {
						ReleBinder binder = DIContainer.Inner.getReleBinder();
						List<BeanVO> tmpList = binder.getBeanVoList();
						for (BeanVO bvo : tmpList) {
							Method m = bvo.getAopMethod();
							if (m != null && m.equals(method)) {
								interceptor = bvo.getInterceptor();
								CACHE.put(method, interceptor);
								break;
							}
						}
					}
				}
			}
			return interceptor;
		}

	}

	/**
	 * 在被拦截方法执行之前，执行此方法（事件）
	 * 
	 * @param methodName
	 *            --被拦截方法名称
	 * @param args
	 *            --被拦截方法的输入参数
	 * @throws Throwable
	 */
	protected abstract void before(String methodName, Object[] args)
			throws Throwable;

	/**
	 * 被拦截方法执行完毕后，执行此方法（事件）
	 * 
	 * @param returnValue
	 *            --被拦截方法执行后返回的结果
	 * @param methodName
	 *            --被拦截方法名称
	 * @param args
	 *            --被拦截方法的输入参数
	 * @throws Throwable
	 */
	protected abstract void after(Object returnValue, String methodName,
			Object[] args) throws Throwable;

}
