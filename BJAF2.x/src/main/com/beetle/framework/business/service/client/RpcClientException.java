package com.beetle.framework.business.service.client;

import com.beetle.framework.AppRuntimeException;

public class RpcClientException extends AppRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RpcClientException(int errCode, String message, Throwable cause) {
		super(errCode, message, cause);
	}

	public RpcClientException(int errCode, String message) {
		super(errCode, message);
	}

	public RpcClientException(int errCode, Throwable cause) {
		super(errCode, cause);
	}

	public RpcClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public RpcClientException(String message) {
		super(message);
	}

	public RpcClientException(Throwable cause) {
		super(cause);
	}

}
