package com.beetle.framework.business.common.tst.aop;

import com.beetle.framework.resource.dic.aop.AopManager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 在需要事务保护的方法上面声明：<br>
 * [@Aop(id = "SERVICE-METHOD-WITH-TRANSACTION")]<br>
 * 即可
 */
public class ServiceTransactionRegister {
	private static AtomicBoolean initFlag = new AtomicBoolean(false);

	public static void register() {
		if (!initFlag.compareAndSet(false, true)) {
			return;
		}
		AopManager am = AopManager.getGlobalManager();
		am.bind("SERVICE-METHOD-WITH-TRANSACTION",
				new ServiceTransactionAopInterceptor());
	}

	public static <T> T retrieveService(Class<T> serviceAoped) {
		return AopManager.getGlobalManager().retrieve(serviceAoped);
	}
}
