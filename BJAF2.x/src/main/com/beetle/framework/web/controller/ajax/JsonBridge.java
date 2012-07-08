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

import com.beetle.framework.log.AppLogger;
import com.metaparadigm.jsonrpc.JSONRPCResult;
import com.metaparadigm.jsonrpc.JSONSerializer;
import com.metaparadigm.jsonrpc.SerializerState;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;

public class JsonBridge implements Serializable {
	public static JsonBridge getInstance() {
		return instance;
	}

	private static final long serialVersionUID = 1L;

	private final static AppLogger log = AppLogger
			.getInstance(JsonBridge.class);
	private final static String ajaxProxyName = "AjaxProxy.execute";
	private final static String id_str = "id";
	private final static String method_str = "method";
	private final static String sys_method_str = "system.listMethods";
	private final static String params_str = "params";
	// JSONSerializer instance for this bridge
	private JSONSerializer ser = new JSONSerializer();
	private static JsonBridge instance = new JsonBridge();

	private JsonBridge() {
		try {
			ser.registerDefaultSerializers();
		} catch (Exception e) {
			log.error(e);
		}
	}

	public JSONRPCResult call(JSONObject jsonReq, AjaxProxy proxy) {
		JSONRPCResult result;
		Object requestId = jsonReq.opt(id_str);
		String encodedMethod = jsonReq.getString(method_str);
		if (encodedMethod.equals(sys_method_str)) {
			JSONArray methods = new JSONArray();
			methods.put(ajaxProxyName);
			return new JSONRPCResult(JSONRPCResult.CODE_SUCCESS, requestId,
					methods);
		}
		JSONArray arguments = jsonReq.getJSONArray(params_str);
		try {
			String params = arguments.getString(0);
			if (log.isDebugEnabled()) {
				log.debug("begin---");
				log.debug(params);
			}
			Map<?, ?> rs = proxy.execute(params);
			if (log.isDebugEnabled()) {
				log.debug("proxy.execute ok");
				log.debug(rs);
				log.debug("end---");
			}
			SerializerState state = new SerializerState();
			result = new JSONRPCResult(JSONRPCResult.CODE_SUCCESS, requestId,
					ser.marshall(state, rs));
		} catch (Exception e) {
			log.error(e);
			result = new JSONRPCResult(JSONRPCResult.CODE_REMOTE_EXCEPTION,
					requestId, e);
		}
		return result;
	}

}
