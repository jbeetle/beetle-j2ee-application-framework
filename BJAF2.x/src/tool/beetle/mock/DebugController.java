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
package beetle.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.Part;

import com.beetle.framework.web.controller.ControllerImp;
import com.beetle.framework.web.controller.WebInput;
import com.beetle.framework.web.view.View;

/**
 * <p>
 * Description: 控制器调试类，完全脱离Servlet容器，提高开发效率
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
public class DebugController {
	private VirRequest request = new VirRequest();
	private Map model;
	private String viewName;
	private boolean createSessionFlag;
	private VirSession session;

	public DebugController() {
		createSessionFlag = false;
		session = null;
	}

	/**
	 * 设置页面参赛，模拟form的field输入
	 * 
	 * @param parameterName
	 *            参数名－－对应form的fieldName
	 * @param value
	 *            值
	 */
	public void setParameter(String parameterName, String value) {
		request.setParameter(parameterName, value);
	}

	public void setSessionValue(String key, Object obj) {
		if (!createSessionFlag) {
			createSessionFlag = true;
			session = new VirSession();
			request.setSession(session);
		}
		session.setAttribute(key, obj);
	}

	/**
	 * 执行调试
	 * 
	 * @param 子控制器
	 */
	public void debug(ControllerImp controller) {
		View m = null;
		try {
			WebInput webInput = new WebInput(request, null);
			m = controller.perform(webInput);
			System.out.println("ViewName:" + m.getViewname());
			System.out.println("------");
			model = m.getData();
			this.viewName = m.getViewname();
			Set entrys = model.entrySet();
			Iterator it = entrys.iterator();
			while (it.hasNext()) {
				Map.Entry e = (Map.Entry) it.next();
				System.out.println("DataName:" + e.getKey());
				System.out.println("DataValue:" + e.getValue());
				System.out.println("--");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		request.clear();
	}

	/**
	 * 获取控制器返回的视图的数据
	 * 
	 * 
	 * @param dataName
	 *            －－数据对应的名称
	 * 
	 * @return Object－－数据值
	 */
	public Object getDataValue(String dataName) {
		if (model == null) {
			throw new com.beetle.framework.AppRuntimeException(
					"run debug method first!");
		}
		return model.get(dataName);
	}

	protected void finalize() throws Throwable {
		if (model != null) {
			model.clear();
		}
		super.finalize();
	}

	public String getViewName() {
		if (viewName == null) {
			throw new com.beetle.framework.AppRuntimeException(
					"run debug method first!");
		}
		return viewName;
	}

	// /

	private static class VirSession implements HttpSession {
		private Map map = new HashMap();

		/**
		 * getAttribute
		 * 
		 * @param string
		 *            String
		 * @return Object
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public Object getAttribute(String string) {
			return map.get(string);
		}

		/**
		 * getAttributeNames
		 * 
		 * @return Enumeration
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public Enumeration getAttributeNames() {
			return null;
		}

		/**
		 * getCreationTime
		 * 
		 * @return long
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public long getCreationTime() {
			return 0L;
		}

		/**
		 * getId
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public String getId() {
			return "";
		}

		/**
		 * getLastAccessedTime
		 * 
		 * @return long
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public long getLastAccessedTime() {
			return 0L;
		}

		/**
		 * getMaxInactiveInterval
		 * 
		 * @return int
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public int getMaxInactiveInterval() {
			return 0;
		}

		/**
		 * getServletContext
		 * 
		 * @return ServletContext
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public ServletContext getServletContext() {
			return null;
		}

		/**
		 * getSessionContext
		 * 
		 * @deprecated
		 * @return HttpSessionContext
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public HttpSessionContext getSessionContext() {
			return null;
		}

		/**
		 * getValue
		 * 
		 * @param string
		 *            String
		 * @return Object
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public Object getValue(String string) {
			return null;
		}

		/**
		 * getValueNames
		 * 
		 * @return String[]
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public String[] getValueNames() {
			return null;
		}

		/**
		 * invalidate
		 * 
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public void invalidate() {
		}

		/**
		 * isNew
		 * 
		 * @return boolean
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public boolean isNew() {
			return false;
		}

		/**
		 * putValue
		 * 
		 * @param string
		 *            String
		 * @param object
		 *            Object
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public void putValue(String string, Object object) {
		}

		/**
		 * removeAttribute
		 * 
		 * @param string
		 *            String
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public void removeAttribute(String string) {
			map.remove(string);
		}

		/**
		 * removeValue
		 * 
		 * @param string
		 *            String
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public void removeValue(String string) {
			map.remove(string);
		}

		/**
		 * setAttribute
		 * 
		 * @param string
		 *            String
		 * @param object
		 *            Object
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public void setAttribute(String string, Object object) {
			map.put(string, object);
		}

		/**
		 * setMaxInactiveInterval
		 * 
		 * @param _int
		 *            int
		 * @todo Implement this javax.servlet.http.HttpSession method
		 */
		public void setMaxInactiveInterval(int _int) {
		}

	}

	private static class VirRequest implements HttpServletRequest {
		private Map datas = new HashMap();
		private VirSession session;

		public VirRequest() {
		}

		public void setSession(VirSession session) {
			this.session = session;
		}

		public int getRemotePort() {
			return 0;
		}

		public void clear() {
			datas.clear();
		}

		public void setParameter(String parameterName, String value) {
			datas.put(parameterName, value);
		}

		/**
		 * getAttribute
		 * 
		 * @param string
		 *            String
		 * @return Object
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public Object getAttribute(String string) {
			return null;
		}

		// public void set
		/**
		 * getAttributeNames
		 * 
		 * @return Enumeration
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public Enumeration getAttributeNames() {
			return null;
		}

		/**
		 * getAuthType
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public String getAuthType() {
			return "";
		}

		/**
		 * getCharacterEncoding
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public String getCharacterEncoding() {
			return "";
		}

		/**
		 * getContentLength
		 * 
		 * @return int
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public int getContentLength() {
			return 0;
		}

		/**
		 * getContentType
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public String getContentType() {
			return "";
		}

		/**
		 * getContextPath
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public String getContextPath() {
			return "";
		}

		/**
		 * getCookies
		 * 
		 * @return Cookie[]
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public Cookie[] getCookies() {
			return null;
		}

		/**
		 * getDateHeader
		 * 
		 * @param string
		 *            String
		 * @return long
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public long getDateHeader(String string) {
			return 0L;
		}

		/**
		 * getHeader
		 * 
		 * @param string
		 *            String
		 * @return String
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public String getHeader(String string) {
			return "";
		}

		/**
		 * getHeaderNames
		 * 
		 * @return Enumeration
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public Enumeration getHeaderNames() {
			return null;
		}

		/**
		 * getHeaders
		 * 
		 * @param string
		 *            String
		 * @return Enumeration
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public Enumeration getHeaders(String string) {
			return null;
		}

		/**
		 * getInputStream
		 * 
		 * @return ServletInputStream
		 * @throws IOException
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public ServletInputStream getInputStream() throws IOException {
			return null;
		}

		/**
		 * getIntHeader
		 * 
		 * @param string
		 *            String
		 * @return int
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public int getIntHeader(String string) {
			return 0;
		}

		/**
		 * getLocale
		 * 
		 * @return Locale
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public Locale getLocale() {
			return null;
		}

		/**
		 * getLocales
		 * 
		 * @return Enumeration
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public Enumeration getLocales() {
			return null;
		}

		/**
		 * getMethod
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public String getMethod() {
			return "";
		}

		/**
		 * getParameter
		 * 
		 * @param string
		 *            String
		 * @return String
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public String getParameter(String parameterName) {
			return (String) datas.get(parameterName);
		}

		/**
		 * getParameterMap
		 * 
		 * @return Map
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public Map getParameterMap() {
			return datas;
		}

		/**
		 * getParameterNames
		 * 
		 * @return Enumeration
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public Enumeration getParameterNames() {
			Vector v = new Vector();
			Iterator it = datas.keySet().iterator();
			while (it.hasNext()) {
				v.add(it.next());
			}
			return v.elements();
		}

		/**
		 * getParameterValues
		 * 
		 * @param string
		 *            String
		 * @return String[]
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public String[] getParameterValues(String parameterName) {
			String a = (String) datas.get(parameterName);
			StringTokenizer st = new StringTokenizer(a, ",");
			String rs[] = new String[st.countTokens()];
			int i = 0;
			while (st.hasMoreTokens()) {
				rs[i] = st.nextToken();
				i++;
			}
			return rs;
		}

		/**
		 * getPathInfo
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public String getPathInfo() {
			return "";
		}

		/**
		 * getPathTranslated
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public String getPathTranslated() {
			return "";
		}

		/**
		 * getProtocol
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public String getProtocol() {
			return "";
		}

		/**
		 * getQueryString
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public String getQueryString() {
			return "";
		}

		/**
		 * getReader
		 * 
		 * @return BufferedReader
		 * @throws IOException
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public BufferedReader getReader() throws IOException {
			return null;
		}

		/**
		 * getRealPath
		 * 
		 * @param string
		 *            String
		 * @return String
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public String getRealPath(String string) {
			return "";
		}

		/**
		 * getRemoteAddr
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public String getRemoteAddr() {
			return "";
		}

		/**
		 * getRemoteHost
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public String getRemoteHost() {
			return "";
		}

		/**
		 * getRemoteUser
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public String getRemoteUser() {
			return "";
		}

		/**
		 * getRequestDispatcher
		 * 
		 * @param string
		 *            String
		 * @return RequestDispatcher
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public RequestDispatcher getRequestDispatcher(String string) {
			return null;
		}

		/**
		 * getRequestURI
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public String getRequestURI() {
			return "";
		}

		/**
		 * getRequestURL
		 * 
		 * @return StringBuffer
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public StringBuffer getRequestURL() {
			return null;
		}

		/**
		 * getRequestedSessionId
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public String getRequestedSessionId() {
			return "";
		}

		/**
		 * getScheme
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public String getScheme() {
			return "";
		}

		/**
		 * getServerName
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public String getServerName() {
			return "";
		}

		/**
		 * getServerPort
		 * 
		 * @return int
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public int getServerPort() {
			return 0;
		}

		/**
		 * getServletPath
		 * 
		 * @return String
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public String getServletPath() {
			return "";
		}

		/**
		 * getSession
		 * 
		 * @return HttpSession
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public HttpSession getSession() {
			return session;
		}

		/**
		 * getSession
		 * 
		 * @param _boolean
		 *            boolean
		 * @return HttpSession
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public HttpSession getSession(boolean _boolean) {
			if (_boolean) {
				if (session == null) {
					session = new VirSession();
				}
			}
			return session;
		}

		/**
		 * getUserPrincipal
		 * 
		 * @return Principal
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public Principal getUserPrincipal() {
			return null;
		}

		/**
		 * isRequestedSessionIdFromCookie
		 * 
		 * @return boolean
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public boolean isRequestedSessionIdFromCookie() {
			return false;
		}

		/**
		 * isRequestedSessionIdFromURL
		 * 
		 * @return boolean
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public boolean isRequestedSessionIdFromURL() {
			return false;
		}

		/**
		 * isRequestedSessionIdFromUrl
		 * 
		 * @return boolean
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public boolean isRequestedSessionIdFromUrl() {
			return false;
		}

		/**
		 * isRequestedSessionIdValid
		 * 
		 * @return boolean
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public boolean isRequestedSessionIdValid() {
			return false;
		}

		/**
		 * isSecure
		 * 
		 * @return boolean
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public boolean isSecure() {
			return false;
		}

		/**
		 * isUserInRole
		 * 
		 * @param string
		 *            String
		 * @return boolean
		 * @todo Implement this javax.servlet.http.HttpServletRequest method
		 */
		public boolean isUserInRole(String string) {
			return false;
		}

		/**
		 * removeAttribute
		 * 
		 * @param string
		 *            String
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public void removeAttribute(String string) {
		}

		/**
		 * setAttribute
		 * 
		 * @param string
		 *            String
		 * @param object
		 *            Object
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public void setAttribute(String string, Object object) {
		}

		/**
		 * setCharacterEncoding
		 * 
		 * @param string
		 *            String
		 * @throws UnsupportedEncodingException
		 * @todo Implement this javax.servlet.ServletRequest method
		 */
		public void setCharacterEncoding(String string)
				throws UnsupportedEncodingException {
		}

		public int getLocalPort() {
			return 0;
		}

		public String getLocalAddr() {
			return "";
		}

		public String getLocalName() {
			return "";
		}

		public AsyncContext getAsyncContext() {
			// TODO Auto-generated method stub
			return null;
		}

		public DispatcherType getDispatcherType() {
			// TODO Auto-generated method stub
			return null;
		}

		public ServletContext getServletContext() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isAsyncStarted() {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isAsyncSupported() {
			// TODO Auto-generated method stub
			return false;
		}

		public AsyncContext startAsync() {
			// TODO Auto-generated method stub
			return null;
		}

		public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean authenticate(HttpServletResponse arg0)
				throws IOException, ServletException {
			// TODO Auto-generated method stub
			return false;
		}

		public Part getPart(String arg0) throws IOException,
				IllegalStateException, ServletException {
			// TODO Auto-generated method stub
			return null;
		}

		public Collection<Part> getParts() throws IOException,
				IllegalStateException, ServletException {
			// TODO Auto-generated method stub
			return null;
		}

		public void login(String arg0, String arg1) throws ServletException {
			// TODO Auto-generated method stub

		}

		public void logout() throws ServletException {
			// TODO Auto-generated method stub

		}
	}

	//
}
