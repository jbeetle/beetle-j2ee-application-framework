package com.beetle.framework.util.pattern.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * AOP方法拦截器 实现拦截方法前后执行功能，<br>
 * 如果before/after不能满足要求，可以重载invoke方法来进行更为灵活的拦截操作。
 */
public abstract class AopInterceptor implements MethodInterceptor {

	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		before(mi.getMethod().getName(), mi.getArguments());
		Object r = mi.proceed();
		after(r, mi.getMethod().getName(), mi.getArguments());
		return r;
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
