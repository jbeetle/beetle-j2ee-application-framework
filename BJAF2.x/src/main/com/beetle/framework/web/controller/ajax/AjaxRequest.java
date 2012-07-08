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
package com.beetle.framework.web.controller.ajax;

import com.beetle.framework.util.ObjectUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

public class AjaxRequest {
	private Map<?, ?> map;
	private HttpServletRequest request;
	private HttpServletResponse response;

	public AjaxRequest(Map<?, ?> map, HttpServletRequest request,
			HttpServletResponse response) {
		this.map = map;
		this.request = request;
		this.response = response;
	}

	/**
	 * 根据名称获取其对应的字符串类型数据
	 * 
	 * 
	 * @param parameterName
	 *            String
	 * @return String
	 */
	public String getParameter(String parameterName) {
		Object o = map.get(parameterName);
		if (o == null) {
			return null;
		} else {
			return o.toString();
		}
	}

	/**
	 * 获取控制器名称
	 * 
	 * 
	 * @return String
	 */
	public String getControllerName() {
		return map.get("AJAX_REQUEST_NAME").toString();
	}

	public float getParameterAsFlt(String name) {
		String r = this.getParameter(name);
		if (r == null) {
			return 0;
		} else if (r.trim().equals("")) {
			return 0;
		}
		return Float.parseFloat(r.trim());
	}

	public Float getParameterAsFloat(String name) {
		String r = this.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		}
		return Float.valueOf(r.trim());
	}

	public Integer getParameterAsInteger(String name) {
		String r = this.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		}
		return Integer.valueOf(r.trim());
	}

	public int getParameterAsInt(String name) {
		String r = this.getParameter(name);
		if (r == null) {
			return 0;
		} else if (r.trim().equals("")) {
			return 0;
		}
		return Integer.parseInt(r.trim());
	}

	public Boolean getParameterAsBoolean(String name) {
		String r = this.getParameter(name);
		return Boolean.valueOf(r);
	}

	public String getRemoteUser() {
		return request.getRemoteHost();
	}

	public String getAuthType() {
		return request.getAuthType();
	}

	public Double getParameterAsDouble(String name) {
		String r = this.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		} else {
			return Double.valueOf(r.trim());
		}
	}

	public double getParameterAsDbl(String name) {
		String r = this.getParameter(name);
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
		String r = this.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		} else {
			return Long.valueOf(r.trim());
		}
	}

	public long getParameterAsLng(String name) {
		String r = this.getParameter(name);
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
		String r = this.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		} else {
			try {
				return Timestamp.valueOf(r.trim());
			} catch (IllegalArgumentException iae) {
				long time = this.getParameterAsLng(name);
				return new Timestamp(time);
			}
		}
	}

	public Time getParameterAsTime(String name) {
		String r = this.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		}

		else {
			return Time.valueOf(r.trim());
		}
	}

	public Date getParameterAsDate(String name) {
		String r = this.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		} else {
			try {
				return Date.valueOf(r.trim());
			} catch (IllegalArgumentException e) {
				long l = this.getParameterAsLng(name);
				return new Date(l);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public Set getParameterNames() {
		return map.keySet();
	}

	@SuppressWarnings("rawtypes")
	public java.util.Map getParameterMap() {
		return this.map;
	}

	/**
	 * 根据名称获取其对应的类对象
	 * 
	 * 
	 * @param name
	 *            值名称
	 * 
	 * @param valueClass
	 *            要自动适配的值对象类
	 * @return 匹配好的值对象
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getParameterAsObject(String name, Class valueClass) {
		Map m = new HashMap();
		JSONObject o2 = getParameterAsJSONObject(name);
		Iterator it = o2.keys();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj != null) {
				m.put(obj, o2.get(obj.toString()));
			}
		}
		try {
			Object obj = valueClass.newInstance();
			Set e = m.entrySet();
			// BeanUtilsBean bu = BeanUtilsBean.getInstance();
			Iterator it2 = e.iterator();
			while (it2.hasNext()) {
				Map.Entry me = (Map.Entry) it2.next();
				ObjectUtil.setValue(me.getKey().toString(), obj, me.getValue());
			}
			// BeanUtils.populate(obj, m);
			return obj;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			o2 = null;
			m.clear();
		}
	}

	/**
	 * 根据名称获取其对应的类对象 需要在"javaClass"属性里面指定其对应的java类
	 * 
	 * 
	 * @param name
	 *            String
	 * @return Object
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getParameterAsObject(String name) {
		Map m = new HashMap();
		JSONObject o2 = getParameterAsJSONObject(name);
		Iterator it = o2.keys();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj != null) {
				m.put(obj, o2.get(obj.toString()));
			}
		}
		String classname = (String) m.get("javaClass");
		try {
			Object obj = Class.forName(classname).newInstance();
			Set e = m.entrySet();
			// BeanUtilsBean bu = BeanUtilsBean.getInstance();
			Iterator it2 = e.iterator();
			while (it2.hasNext()) {
				Map.Entry me = (Map.Entry) it2.next();
				ObjectUtil.setValue(me.getKey().toString(), obj, me.getValue());
			}
			// BeanUtils.populate(obj, m);
			return obj;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			o2 = null;
			m.clear();
		}
	}

	/**
	 * 根据名称返回对象列表，在客户端，如果是非基本类型的对象，需要在"javaClass"属性里面 指定其对应的java类
	 * 
	 * 
	 * @param name
	 *            String
	 * @return List
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getParameterAsList(String name) {
		JSONObject jsonObj = getParameterAsJSONObject(name);
		JSONArray array = jsonObj.getJSONArray("list");
		List al = new ArrayList(array.length());
		for (int i = 0; i < array.length(); i++) {
			try {
				JSONObject o2 = array.getJSONObject(i);
				parstObj(al, o2);
			} catch (java.util.NoSuchElementException e) {
				al.add(array.getString(i)); // 非对象数据，统一转化成字符串
			}
		}
		return al;
	}

	/**
	 * 以数组形式获取client端传递过来的数组数据（javascript Array）
	 * 
	 * 
	 * @param name
	 *            参数名称
	 * @return 对象数组
	 */
	public Object[] getParameterAsArray(String name) {
		JSONArray array;
		try {
			array = new JSONArray(this.getParameter(name));
		} catch (ParseException ex) {
			ex.printStackTrace();
			return null;
		}
		int length = array.length();
		Object ojs[] = new Object[length];
		for (int i = 0; i < length; i++) {
			ojs[i] = array.get(i);
		}
		return ojs;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void parstObj(List al, JSONObject o2) throws NoSuchElementException {
		Map m = new HashMap();
		Iterator it = o2.keys();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj != null) {
				m.put(obj, o2.get(obj.toString()));
			}
		}
		String classname = (String) m.get("javaClass");
		if (classname != null) {
			try {
				Object obj = Class.forName(classname).newInstance();
				Set e = m.entrySet();
				// BeanUtilsBean bu = BeanUtilsBean.getInstance();
				Iterator it2 = e.iterator();
				while (it2.hasNext()) {
					Map.Entry me = (Map.Entry) it2.next();
					// bu.copyProperty(obj, me.getKey().toString(),
					// me.getValue());
					ObjectUtil.setValue(me.getKey().toString(), obj,
							me.getValue());
				}
				// BeanUtils.populate(obj, m);
				al.add(obj);
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new com.beetle.framework.AppRuntimeException(ex);
			} finally {
				o2 = null;
				m.clear();
			}
		} else {
			al.add(o2); // 如果对象不包括指定类，返回json格式对象
			o2 = null;
			m.clear();
		}
	}

	/**
	 * 根据名称返回json格式的对象 需要自己解析
	 * 
	 * 
	 * @param name
	 *            String
	 * @return JSONObject
	 */
	public JSONObject getParameterAsJSONObject(String name) {
		try {
			return new JSONObject(this.getParameter(name));
		} catch (ParseException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public Principal getUserPrincipal() {
		return request.getUserPrincipal();
	}

	public boolean isUserInRole(java.lang.String role) {
		return request.isUserInRole(role);
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

	/**
	 * Called by the garbage collector on an object when garbage collection
	 * determines that there are no more references to the object.
	 * 
	 * @throws Throwable
	 *             the <code>Exception</code> raised by this method
	 * @todo Implement this java.lang.Object method
	 */
	protected void finalize() throws Throwable {
		if (!map.isEmpty()) {
			map.clear();
		}
		super.finalize();
	}
}
