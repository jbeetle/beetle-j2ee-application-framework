package com.beetle.framework.util.pattern.di;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.locks.ReentrantLock;

/**
 * DI容器
 * 
 * @author HenryYu
 * 
 */
public final class DIContainer {
	private final Injector injector;
	private static DIContainer instance;
	private static final ReentrantLock lock = new ReentrantLock();

	public static DIContainer getGlobalDI(InputStream binderfile) {
		if (instance == null) {
			try {
				lock.lock();
				if (instance == null) {
					ReleBinder rb = new ReleBinder();
					rb.bindFromConfig(binderfile);
					instance = new DIContainer(rb);
				}
			} finally {
				lock.unlock();
			}
		}
		return instance;
	}

	public static DIContainer getGlobalDI(File binderfile) {
		if (instance == null) {
			try {
				lock.lock();
				if (instance == null) {
					ReleBinder rb = new ReleBinder();
					rb.bindFromConfig(binderfile);
					instance = new DIContainer(rb);
				}
			} finally {
				lock.unlock();
			}
		}
		return instance;
	}

	public DIContainer(ReleBinder rb) {
		injector = Guice.createInjector(rb.getModuler());
	}

	/**
	 * 根据客户端类获取一个客户端实例（通过inject方式获取服务工作的类）
	 * 
	 * @param <T>
	 * @param serviceUser
	 *            （引用服务的客户端类）
	 * @return
	 */
	public <T> T retrieve(Class<T> serviceUser) {
		return injector.getInstance(serviceUser);
	}

}
