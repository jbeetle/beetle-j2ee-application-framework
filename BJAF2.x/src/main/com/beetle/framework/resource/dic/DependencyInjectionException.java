package com.beetle.framework.resource.dic;

import com.beetle.framework.AppRuntimeException;

public class DependencyInjectionException extends AppRuntimeException {

	private static final long serialVersionUID = 1L;

	public DependencyInjectionException(int errCode, String message,
			Throwable cause) {
		super(errCode, message, cause);
	}

	public DependencyInjectionException(int errCode, String message) {
		super(errCode, message);
	}

	public DependencyInjectionException(int errCode, Throwable cause) {
		super(errCode, cause);
	}

	public DependencyInjectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public DependencyInjectionException(String message) {
		super(message);
	}

	public DependencyInjectionException(Throwable cause) {
		super(cause);
	}

}
