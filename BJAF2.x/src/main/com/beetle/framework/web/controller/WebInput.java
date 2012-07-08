/*
 * BJAF - Beetle J2EE Application Framework
 * 甲壳虫J2EE企业应用开发框架
 * 版权所有2003-2015 余浩东 (www.beetlesoft.net)
 * 
 * 这是一个免费开源的软件，您必须在
 *<http://www.apache.org/licenses/LICENSE-2.0>
 *协议下合法使用、修改或重新发布。
 *
 * 感谢您使用、推广本框架，若有建议或问题，欢迎您和我联系。
 * 邮件： <yuhaodong@gmail.com/>.
 */
package com.beetle.framework.web.controller;

import com.beetle.framework.AppRuntimeException;
import com.beetle.framework.util.ObjectUtil;
import com.beetle.framework.web.common.CommonUtil;
import com.beetle.framework.web.common.WebUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.Principal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Title: BeetleWeb
 * </p>
 * 
 * <p>
 * Description: Web输入参数对象，对request的封装
 * 
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company: 甲壳虫软件
 * 
 * </p>
 * 
 * @author 余浩东(hdyu@beetlesoft.net)
 * @version 1.0
 */
public class WebInput {
	private HttpServletRequest request;

	private HttpServletResponse response;

	public WebInput(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		/*
		 * Object o = request.getAttribute("WEB_ENCODE_CHARSET"); if (o == null)
		 * { o = System.getProperty("file.encoding"); } try {
		 * this.request.setCharacterEncoding(o.toString()); } catch
		 * (UnsupportedEncodingException ex) { ex.printStackTrace(); }
		 */
	}

	/**
	 * 取消控制器的Session检查功能,此方法提供了一个在原有控制器已设置了Session检查的情况下，
	 * 针对某个请求，取消此控制器Session检查功能的可能。我们一般会在前置回调ICutFrontAction中使用
	 */
	public void disableControllerSessionCheck() {
		request.setAttribute(CommonUtil.CANCEL_SESSION_CHECK_FLAG, "yesCancel");
	}

	public Principal getUserPrincipal() {
		return request.getUserPrincipal();
	}

	public boolean isUserInRole(java.lang.String role) {
		return request.isUserInRole(role);
	}

	/**
	 * 获取当然控制器的名称
	 * 
	 * @return String
	 */
	public String getControllerName() {
		return (String) request.getAttribute(CommonUtil.controllname);
	}

	public String getAuthType() {
		return request.getAuthType();
	}

	public String getServletPath() {
		return request.getServletPath();
	}

	public String getRemoteUser() {
		return request.getRemoteHost();
	}

	/**
	 * 返回控制器的动作名称（针对多动作控制器）
	 * 
	 * @return
	 */
	public String getActionName() {
		String a = request.getParameter(CommonUtil.ACTION_STR);
		if (a == null || a.length() < 1) {
			a = "default";
		}
		return a;
	}

	public String getRemoteAddr() {
		return request.getRemoteAddr();
	}

	public String getRemoteHost() {
		return request.getRemoteHost();
	}

	public boolean isSecure() {
		return request.isSecure();
	}

	public String getCharacterEncoding() {
		return request.getCharacterEncoding();
	}

	public String getSessionId() {
		HttpSession ss = this.getSession();
		if (ss == null) {
			return null;
		} else {
			return ss.getId();
		}
	}

	public Cookie getCookie(String cookieName) {
		Cookie cks[] = getCookies();
		if (cks == null) {
			return null;
		} else {
			for (int i = 0; i < cks.length; i++) {
				Cookie ck = cks[i];
				String name = ck.getName();
				if (name.equals(cookieName)) {
					return ck;
				}
			}
			return null;
		}
	}

	/**
	 * 获取验证码值 与com.beetle.framework.web.draw.VerifyCodeDraw一起使用
	 * 
	 * 
	 * @return String
	 */
	public String getVerifyCode() {
		Cookie ck = getCookie("VerifyCodeDraw");
		if (ck != null) {
			return ck.getValue();
		} else {
			return null;
		}
	}

	/**
	 * Adds the specified cookie to the response. This method can be called
	 * multiple times to set more than one cookie.
	 * 
	 * @param cookie
	 *            cookie - the Cookie to return to the client
	 */
	public void addCookie(Cookie cookie) {
		response.addCookie(cookie);
	}

	/**
	 * Returns an array containing all of the Cookie objects the client sent
	 * with this request. This method returns null if no cookies were sent.
	 * 
	 * @return Cookie[]
	 */
	public Cookie[] getCookies() {
		return request.getCookies();
	}

	/**
	 * Returns the value of the specified request header as a String. If the
	 * request did not include a header of the specified name, this method
	 * returns null. The header name is case insensitive. You can use this
	 * method with any request header.
	 * 
	 * @param name
	 *            String
	 * @return String
	 */
	public String getHeader(String name) {
		return request.getHeader(name);
	}

	/**
	 * Returns the current session associated with this request, or if the
	 * request does not have a session, creates one.
	 * 
	 * @return HttpSession
	 */
	public HttpSession getSession() {
		return request.getSession();
	}

	/**
	 * Returns the current HttpSession associated with this request or, if if
	 * there is no current session and create is true, returns a new session. If
	 * create is false and the request has no valid HttpSession, this method
	 * returns null. To make sure the session is properly maintained, you must
	 * call this method before the response is committed. If the container is
	 * using cookies to maintain session integrity and is asked to create a new
	 * session when the response is committed, an IllegalStateException is
	 * thrown.
	 * 
	 * @param create
	 *            boolean
	 * @return HttpSession
	 */
	public HttpSession getSession(boolean create) {
		return request.getSession(create);
	}

	/**
	 * request. For HTTP servlets, parameters are contained in the query string
	 * or posted form data. You should only use this method when you are sure
	 * the parameter has only one value. If the parameter might have more than
	 * one value, use getParameterValues(java.lang.String). If you use this
	 * method with a multivalued parameter, the value returned is equal to the
	 * first value in the array returned by getParameterValues. If the parameter
	 * data was sent in the request body, such as occurs with an HTTP POST
	 * request, then reading the body directly via getInputStream() or
	 * getReader() can interfere with the execution of this method.
	 * 注意使用本方法会自动trim()调字符串前后的空格，如果你想保留可以使用getParameterWithoutTrim()方法
	 * 
	 * @name - a String specifying the name of the parameter
	 * @return a String representing the single value of the parameter
	 */
	public String getParameter(String name) {
		String r = request.getParameter(name);
		if (request.getMethod().toLowerCase().equals(CommonUtil.GET_STR)) {
			String info = (String) request
					.getAttribute(CommonUtil.WEB_SERVER_INFO);
			if (info != null) {
				if (info.indexOf(CommonUtil.TOMCAT_STR) > 0) {
					try {
						if (r != null) {
							r = new String(r.getBytes("8859_1"));
						}
					} catch (UnsupportedEncodingException ex) {
						r = request.getParameter(name);
					}
				}
			}
		}
		if (r != null) {
			r = r.trim();
		}
		return r;
	}

	/**
	 * 根据name获取参数值（trim过），如果此值为null，则返回输入参数的默认值
	 * 
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public String getParameter(String name, String defaultValue) {
		String x = getParameter(name);
		if (x == null) {
			return defaultValue;
		}
		return x;
	}

	public String getParameterWithoutTrim(String name) {
		return request.getParameter(name);
	}

	/**
	 * 以当前系统默认文件编码解码
	 * 
	 * @param name
	 * @return
	 */
	public String getParameterWithDecode(String name) {
		return WebUtil.decodeURL(this.getParameter(name));
	}

	public String getParameterWithDecode(String name, String charset) {
		return WebUtil.decodeURL(this.getParameter(name), charset);
	}

	public float getParameterAsFlt(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return 0;
		} else if (r.trim().equals("")) {
			return 0;
		}
		return Float.parseFloat(r.trim());
	}

	public Float getParameterAsFloat(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		}
		return Float.valueOf(r.trim());
	}

	public Integer getParameterAsInteger(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		}
		return Integer.valueOf(r.trim());
	}

	public int getParameterAsInt(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return 0;
		} else if (r.trim().equals("")) {
			return 0;
		}
		return Integer.parseInt(r.trim());
	}

	public Double getParameterAsDouble(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		} else {
			return Double.valueOf(r.trim());
		}
	}

	public double getParameterAsDbl(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return 0;
		} else if (r.trim().equals("")) {
			return 0;
		}

		else {
			return Double.parseDouble(r.trim());
		}
	}

	public Long getParameterAsLong(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		} else {
			return Long.valueOf(r.trim());
		}
	}

	public long getParameterAsLng(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return 0;
		} else if (r.trim().equals("")) {
			return 0;
		} else {
			return Long.parseLong(r.trim());
		}
	}

	// yyyy-mm-dd hh:mm:ss.fffffffff
	public Timestamp getParameterAsTimestamp(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		} else {
			String a = r.trim();
			if (a.indexOf(':') < 0) {
				a = a + " 00:00:00";
			}
			return Timestamp.valueOf(a);
		}
	}

	public BigDecimal getParameterAsBigDecimal(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		} else {
			return new BigDecimal(r.trim());
		}
	}

	public Time getParameterAsTime(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		}

		else {
			return Time.valueOf(r.trim());
		}
	}

	public java.sql.Date getParameterAsDate(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		}

		else {
			return java.sql.Date.valueOf(r.trim());
		}
	}

	/**
	 * eturns a java.util.Map of the parameters of this request. Request
	 * parameters are extra information sent with the request. For HTTP
	 * servlets, parameters are contained in the query string or posted form
	 * data.
	 * 
	 * @return an immutable java.util.Map containing parameter names as keys and
	 *         parameter values as map values. The keys in the parameter map are
	 *         of type String. The values in the parameter map are of type
	 *         String array
	 */
	@SuppressWarnings("rawtypes")
	public java.util.Map getParameterMap() {
		return request.getParameterMap();
	}

	/**
	 * Returns an Enumeration of String objects containing the names of the
	 * parameters contained in this request. If the request has no parameters,
	 * the method returns an empty Enumeration.
	 * 
	 * @return Enumeration
	 */
	@SuppressWarnings("rawtypes")
	public java.util.Enumeration getParameterNames() {
		return request.getParameterNames();
	}

	/**
	 * Returns an array of String objects containing all of the values the given
	 * request parameter has, or null if the parameter does not exist. If the
	 * parameter has a single value, the array has a length of 1.
	 * 
	 * @paramn - a String containing the name of the parameter whose value is
	 *         requested
	 * @return an array of String objects containing the parameter's values
	 */
	public String[] getParameterValues(String name) {
		return request.getParameterValues(name);
	}

	/**
	 * 为了免除每次都通过vgetParametervAsXXX方法获取页面输入的field值 我们可以针对html form表单建立一个form
	 * bean，通过此方法可以自动填充页面的各个输入参数，以formbean的对象返回。 (注：formbean的属性名称必须和页面form的field
	 * name保持一致)
	 * 
	 * @param formBeanClass
	 *            formbean值对象对应的类Class
	 * @return 返回填充好数据的formbean值对象
	 */
	public <T> T getParameterValuesAsFormBean(Class<T> formBeanClass) {
		T obj;
		try {
			obj = formBeanClass.newInstance();
			Map<?, ?> m = getParameterMap();
			Set<?> e = m.entrySet();
			Iterator<?> it = e.iterator();
			while (it.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map.Entry me = (Map.Entry) it.next();
				String key = (String) me.getKey();
				String values[] = getParameterValues(key);
				if (values.length == 1) {
					try {
						ObjectUtil.setValue(key, obj, values[0]);
					} catch (IllegalArgumentException ille) {
						Class<?> type = ObjectUtil.getType(key, obj);
						String tstr = type.toString();
						// System.out.println("---->:"+tstr);
						// System.out.println("key="+key+"
						// value="+this.getParameter(key));
						if (tstr.equals(Integer.class.toString())) {
							ObjectUtil.setValue(key, obj,
									getParameterAsInteger(key));
						} else if (tstr.equals(Long.class.toString())) {
							ObjectUtil.setValue(key, obj,
									getParameterAsLong(key));
						} else if (tstr.equals(Float.class.toString())) {
							ObjectUtil.setValue(key, obj,
									getParameterAsFloat(key));
						} else if (tstr.equals(Double.class.toString())) {
							ObjectUtil.setValue(key, obj,
									getParameterAsDouble(key));
						} else if (tstr.equals(Timestamp.class.toString())) {
							ObjectUtil.setValue(key, obj,
									getParameterAsTimestamp(key));
						} else if (tstr.equals(java.sql.Date.class.toString())) {
							ObjectUtil.setValue(key, obj,
									getParameterAsDate(key));
						} else if (tstr.equals(BigDecimal.class.toString())) {
							ObjectUtil.setValue(key, obj,
									getParameterAsBigDecimal(key));
						} else {
							com.beetle.framework.log.AppLogger
									.getInstance(this.getClass())
									.warn("["
											+ key
											+ "]not support,please deal it yourselef!");
							ille.printStackTrace();
						}
						tstr = null;
						type = null;
					}
				} else {
					try {
						ObjectUtil.setValue(key, obj, values);
					} catch (IllegalArgumentException ille) {
						ille.printStackTrace();
						com.beetle.framework.log.AppLogger
								.getInstance(this.getClass())
								.warn("["
										+ key
										+ "]not support,please deal it yourselef!");
					}
				}
			}
		} catch (Throwable ex) {
			throw new AppRuntimeException(ex);
		}
		return obj;
	}

	/**
	 * 从session中取值
	 * 
	 * 
	 * @param valueName
	 *            名称
	 * @return 对应的值Object
	 */
	public Object getDataFromSession(String valueName) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		} else {
			return session.getAttribute(valueName);
		}
	}

	HttpServletRequest getRequest() {
		return request;
	}

	HttpServletResponse getResponse() {
		return response;
	}

	public InputStream getInputStream() throws IOException {
		return request.getInputStream();
	}
}
