package com.beetle.framework.business.service.common;

public class RpcResponse implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int returnFlag;
	private String returnMsg;
	private Object result;
	private Object exception;
	private boolean async;
	
	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	
	@Override
	public String toString() {
		return "RpcResponse [returnFlag=" + returnFlag + ", returnMsg="
				+ returnMsg + ", result=" + result + ", exception=" + exception
				+ ", async=" + async + "]";
	}

	public RpcResponse() {
		super();
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public int getReturnFlag() {
		return returnFlag;
	}

	public void setReturnFlag(int returnFlag) {
		this.returnFlag = returnFlag;
	}

	public String getReturnMsg() {
		return returnMsg;
	}

	public void setReturnMsg(String returnMsg) {
		this.returnMsg = returnMsg;
	}

	public Object getException() {
		return exception;
	}

	public void setException(Object exception) {
		this.exception = exception;
	}

}
