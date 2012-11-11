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

import java.text.ParseException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.beetle.framework.log.AppLogger;
import com.beetle.framework.util.ClassUtil;
import com.beetle.framework.util.cache.ICache;
import com.beetle.framework.util.cache.StrongCache;
import com.beetle.framework.web.common.CommonUtil;

public class AjaxProxy implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient HttpServletRequest request;

	private transient HttpServletResponse response;
	private static final AppLogger logger = AppLogger
			.getInstance(AjaxProxy.class);

	public AjaxProxy() {

	}

	private static ICache actionCache = new StrongCache();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map strToMap(String requestParameter)
			throws NoSuchElementException, ParseException {
		Map params = new HashMap();
		JSONObject o = new JSONObject(requestParameter);
		Object m = o.get("map");
		JSONObject o2 = new JSONObject(m.toString());
		Iterator it = o2.keys();
		while (it.hasNext()) {
			Object obj = it.next();
			params.put(obj, o2.get(obj.toString()));
		}
		o = null;
		o2 = null;
		return params;
	}

	private static final String AJAX_REQUEST_NAME = "AJAX_REQUEST_NAME";
	private static final String GLOBAL_FRONTCALL_FLAG = "GLOBAL_FRONTCALL_FLAG";
	private static final String GLOBAL_BACKCALL_FLAG = "GLOBAL_BACKCALL_FLAG";

	/*
	 * requestParameter example format: "{'javaClass': 'java.util.Hashtable',
	 * 'map': {'AJAX_REQUEST_NAME':'xxx.ajax','name': 'Henry', 'email':
	 * 'henryyu@163.com', '1': {'foo': 'foo 1', 'javaClass': 'Example.Wiggle',
	 * 'bar': 1}}}";
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map execute(String requestParameter) throws Exception {
		Map rM = new Hashtable();
		Map params = strToMap(requestParameter); // ת���ɲ���Map
		Map config = AjaxConfig.getAjaxConfig((ServletContext) this.request
				.getAttribute(CommonUtil.app_Context));
		String key = params.get(AJAX_REQUEST_NAME).toString();
		logger.debug("key:{}", key);
		String actionClassName = (String) config.get(key);
		Object actionObj;
		if (actionClassName == null) {// 在配置文件中找不到，按0配置方式找
			actionClassName = (String) this.request
					.getAttribute(CommonUtil.WEB_CTRL_PREFIX);
			if (actionClassName == null) {
				actionClassName = CommonUtil.formatPath(key);
			} else {
				actionClassName = CommonUtil.delLastBevel(actionClassName)
						+ "." + CommonUtil.formatPath(key);
			}
			logger.debug("actionClassName:{}", actionClassName);
			config.put(key, actionClassName);
		}
		logger.debug("config:{}", config);
		actionObj = this.request.getAttribute("AJAX_CTRL_IOBJ");
		if (actionObj == null) {
			actionObj = actionCache.get(key);
			if (actionObj == null) {
				try {
					actionObj = Class.forName(actionClassName).newInstance();
					if (ClassUtil.isThreadSafe(actionObj.getClass())) {
						actionCache.put(key, actionObj);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					params.clear();
					throw ex;
				}
			}
		}
		AjaxResponse aresponse = null;
		AjaxRequest arequest = new AjaxRequest(params, this.request,
				this.response);
		if (arequest.getParameterAsBoolean(GLOBAL_FRONTCALL_FLAG)
				.booleanValue()) {
			ICommonAjax precall = AjaxConfig.getPreCall();
			if (precall != null) {
				aresponse = precall.perform(arequest);
				if (aresponse.isBreakFlag()) {
					setMap(rM, aresponse);
					params.clear();
					aresponse.clear();
					return rM; // ֱ�ӷ���
				} else {
					rM.putAll(aresponse);
					aresponse.clear();
				}
			}
		}
		try {
			ICommonAjax comAjax = (ICommonAjax) actionObj;
			aresponse = comAjax.perform(arequest);
			setMap(rM, aresponse);
			if (arequest.getParameterAsBoolean(GLOBAL_BACKCALL_FLAG)
					.booleanValue()) {
				ICommonAjax backCall = AjaxConfig.getBackCall();
				if (backCall != null) {
					aresponse = backCall.perform(arequest);
					if (aresponse.isBreakFlag()) {
						setMap(rM, aresponse);
						params.clear();
						aresponse.clear();
						return rM; // ֱ�ӷ���
					} else {
						rM.putAll(aresponse);
					}
				}
			}
			return rM;
		} finally {
			params.clear();
			if (aresponse != null) {
				aresponse.clear();
			}
		}
		// params.clear();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setMap(Map rM, AjaxResponse aresponse) {
		rM.put("ReturnMsg", aresponse.getReturnMsg());
		rM.put("ReturnFlag", aresponse.getReturnFlag());
		rM.putAll(aresponse);
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}
	/*
	 * public static void main(String arg[]) throws ParseException { String a =
	 * "{'javaClass': 'java.util.Hashtable', 'map': {'name': 'Henry', 'email':
	 * 'henryyu@163.com', 'myObj': {'foo': 'foo 1', 'javaClass':
	 * 'Example.Wiggle', 'bar': 1}}}"; Map m = strToMap(a); AjaxRequest ax = new
	 * AjaxRequest(m); System.out.println(ax.getParameter("name"));
	 * Example.Wiggle w = (Example.Wiggle) ax.getParameterAsObject("myObj");
	 * System.out.println(w.getBar()); System.out.println(w.getFoo()); }
	 */
}
