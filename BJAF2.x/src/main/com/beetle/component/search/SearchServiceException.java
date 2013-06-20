package com.beetle.component.search;

import com.beetle.framework.AppException;

public class SearchServiceException extends AppException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SearchServiceException(int errCode, String message, Throwable cause) {
		super(errCode, message, cause);
	}

	public SearchServiceException(int errCode, String message) {
		super(errCode, message);
	}

	public SearchServiceException(int errCode, Throwable cause) {
		super(errCode, cause);
	}

	public SearchServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public SearchServiceException(String message) {
		super(message);
	}

	public SearchServiceException(Throwable cause) {
		super(cause);
	}

}
