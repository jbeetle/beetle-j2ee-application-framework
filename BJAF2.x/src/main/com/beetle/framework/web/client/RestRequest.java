package com.beetle.framework.web.client;

import java.util.HashMap;
import java.util.Map;

public final class RestRequest {

	/**
	 * 请求参数对象构造函数
	 * @param url--请求服务地址
	 * @param invokeMethod--http执行方法
	 */
	public RestRequest(String url, InvokeMethod invokeMethod) {
		super();
		this.url = url;
		this.invokeMethod = invokeMethod;
		this.headerMap = new HashMap<String, String>();
		this.paramMap = new HashMap<String, String>();
	}

	String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	InvokeMethod getInvokeMethod() {
		return invokeMethod;
	}

	Map<String, String> getHeaderMap() {
		return headerMap;
	}

	Map<String, String> getParamMap() {
		return paramMap;
	}

	public enum InvokeMethod {
		GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS;
	}

	/**
	 * 在请求前，往header加入参数
	 * 
	 * @param key
	 * @param value
	 */
	public void addHeader(String key, String value) {
		headerMap.put(key, value);
	}

	/**
	 * 添加请求参数
	 * 
	 * @param key
	 *            --参数名称
	 * @param value
	 *            --参数值
	 */
	public void addParameter(String key, String value) {
		paramMap.put(key, value);
	}

	void clear() {
		this.headerMap.clear();
		this.paramMap.clear();
	}

	private String url;
	private final InvokeMethod invokeMethod;
	private final Map<String, String> headerMap;
	private final Map<String, String> paramMap;
}
