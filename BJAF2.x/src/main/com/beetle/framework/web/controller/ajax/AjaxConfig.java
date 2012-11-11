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

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import com.beetle.framework.AppRuntimeException;
import com.beetle.framework.util.file.XMLReader;
import com.beetle.framework.web.common.CommonUtil;
import com.beetle.framework.web.common.WebConst;
import com.beetle.framework.web.controller.ControllerFactory;

public class AjaxConfig {
	private static Map<String, String> ajaxTable = new ConcurrentHashMap<String, String>();

	private static String GlobalBackCallClassName = null;

	private static String GlobalPreCallClassName = null;

	private static volatile ICommonAjax preCall = null;

	private static volatile ICommonAjax backCall = null;

	static final String AJAX_FRAMEWORK_NAME = "BEETLE_AJAX_FRAMEWORK";
	private final static Object locker = new Object();

	private static void loadTable(ServletContext app) {
		if (!ajaxTable.isEmpty()) {
			return;
		}
		//
		synchronized (locker) {
			CommonUtil.fill_DataMap(app, WebConst.WEB_CONTROLLER_FILENAME,
					"mappings.controllers.ajax", "aItem", "name", "class",
					ajaxTable);
			Map<?, ?> mItem = ControllerFactory.getModuleItem(app);
			// 加载其它文件的数据
			if (!mItem.isEmpty()) {
				Set<?> s = mItem.entrySet();
				Iterator<?> it = s.iterator();
				while (it.hasNext()) {
					@SuppressWarnings("rawtypes")
					Map.Entry e = (Map.Entry) it.next();
					String fn = (String) e.getKey();
					String active = (String) e.getValue();
					if (active.equalsIgnoreCase("true")) {
						CommonUtil.fill_DataMap(app, fn,
								"mappings.controllers.ajax", "aItem", "name",
								"class", ajaxTable);
					}
				}
			}
		}
	}

	public final static boolean isAjaxController(String url) {
		return url.startsWith(AJAX_FRAMEWORK_NAME);
	}

	public static Map<String, String> getAjaxConfig(ServletContext app) {
		if (ajaxTable.isEmpty()) {
			loadTable(app);
			GlobalBackCallClassName = getGlobalBackCallStr(app);
			GlobalPreCallClassName = getGlobalPreCallStr(app);
		}
		return ajaxTable;
	}

	private static String getGlobalBackCallStr(ServletContext application) {
		InputStream in2;
		in2 = application.getResourceAsStream(WebConst.WEB_CONTROLLER_FILENAME);
		String a = XMLReader.getTagContent(in2,
				"mappings.controllers.cutting.ajaxBackAction");
		return a;
	}

	private static String getGlobalPreCallStr(ServletContext application) {
		InputStream in2;
		in2 = application.getResourceAsStream(WebConst.WEB_CONTROLLER_FILENAME);
		String precallName = XMLReader.getTagContent(in2,
				"mappings.controllers.cutting.ajaxFrontAction");
		return precallName;
	}

	public static ICommonAjax getPreCall() {
		if (preCall == null) {
			if (GlobalPreCallClassName == null
					|| GlobalPreCallClassName.equals("")) {
				return null;
			}
			try {
				preCall = (ICommonAjax) Class.forName(
						GlobalPreCallClassName.trim()).newInstance();
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new AppRuntimeException(ex.getMessage());
			}
		}
		return preCall;
	}

	public static ICommonAjax getBackCall() {
		if (backCall == null) {
			if (GlobalBackCallClassName == null
					|| GlobalBackCallClassName.equals("")) {
				return null;
			}
			try {
				backCall = (ICommonAjax) Class.forName(
						GlobalBackCallClassName.trim()).newInstance();
			} catch (Exception ex) {
				ex.printStackTrace();
				throw new AppRuntimeException(ex.getMessage());
			}
		}
		return backCall;
	}

}
