package com.beetle.framework.resource.dic.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.beetle.framework.resource.dic.DIContainer;
import com.beetle.framework.resource.dic.ReleBinder;
import com.beetle.framework.resource.dic.ReleBinder.BeanVO;
import com.beetle.framework.resource.dic.def.ServiceTransaction;

public class InnerHandler implements InvocationHandler {
	private final Class<?> targetImpFace;
	private static final Map<Method, AopInterceptor> CACHE = new ConcurrentHashMap<Method, AopInterceptor>();

	public InnerHandler(Class<?> targetImpFace) {
		super();
		this.targetImpFace = targetImpFace;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		AopInterceptor interceptor = getInterceptor(method);
		if (interceptor != null) {
			if (interceptor.interrupt()) {
				return interceptor.interruptResult(proxy, method, args);
			}
			interceptor.before(method, args);
		}
		//
		Object targetImp = DIContainer.Inner
				.getBeanFromDIBeanCache(targetImpFace.getName());
		Object rs = null;
		if (BeanVO.existInTrans(method)) {
			ServiceTransaction.Manner manner = BeanVO.getFromTrans(method);
			if (manner.equals(ServiceTransaction.Manner.REQUIRED)) {
				rs = com.beetle.framework.business.common.tst.aop.ServiceTransactionInterceptor
						.invokeRequired(targetImp, method, args);
			} else if (manner.equals(ServiceTransaction.Manner.REQUIRES_NEW)) {
				rs = com.beetle.framework.business.common.tst.aop.ServiceTransactionInterceptor
						.invokeRequiresNew(targetImp, method, args);
			}
		} else {
			rs = method.invoke(targetImp, args);
		}
		//
		if (interceptor != null) {
			interceptor.after(rs, method, args);
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
