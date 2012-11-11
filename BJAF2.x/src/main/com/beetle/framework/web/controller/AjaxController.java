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

import com.beetle.framework.log.AppLogger;
import com.beetle.framework.web.common.WebConst;
import com.beetle.framework.web.controller.ajax.AjaxProxy;
import com.beetle.framework.web.controller.ajax.JsonBridge;
import com.metaparadigm.jsonrpc.JSONRPCResult;
import org.json.JSONObject;

import java.io.*;

public class AjaxController extends AbnormalViewControlerImp {
	public AjaxController() {
		this.setCacheSeconds(-1);
		this.disableBackAction();
		this.disableFrontAction();
	}

	private final static int buf_size = 4096;

	private static AppLogger logger = AppLogger
			.getInstance(AjaxController.class);

	public void performX(WebInput webInput, OutputStream out)
			throws ControllerException {
		String charset = (String) webInput.getCharacterEncoding();// request.getAttribute("WEB_ENCODE_CHARSET");
		if (charset == null) {
			charset = "UTF-8";
		}
		if (logger.isDebugEnabled()) {
			logger.debug("charset:" + charset);
		}
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					webInput.getInputStream(), charset));
			// Read the request
			CharArrayWriter data = new CharArrayWriter();
			char buf[] = new char[buf_size];
			int ret;
			while ((ret = in.read(buf, 0, buf_size)) != -1) {
				data.write(buf, 0, ret);
			}
			// Process the request
			JSONRPCResult json_res = proxyCall(webInput, data);
			byte[] bout = json_res.toString().getBytes(charset);
			if (logger.isDebugEnabled()) {
				logger.debug("response data:" + json_res.toString());
			}
			out.write(bout);
		} catch (Exception e) {
			throw new ControllerException(WebConst.WEB_EXCEPTION_CODE_AJAX,
					e.getMessage(), e);
		} finally {
			try {
				out.flush();
				out.close();
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	private JSONRPCResult proxyCall(WebInput webInput, CharArrayWriter data) {
		JsonBridge json_bridge = JsonBridge.getInstance();
		JSONObject json_req;
		JSONRPCResult json_res;
		try {
			json_req = new JSONObject(data.toString());
			if (logger.isDebugEnabled()) {
				logger.debug("JsonBridge");
				logger.debug("request data:" + data.toString());
			}
			
			AjaxProxy proxyObj = new AjaxProxy();
			proxyObj.setRequest(webInput.getRequest());
			proxyObj.setResponse(webInput.getResponse());
			json_res = json_bridge.call(json_req, proxyObj);
		} catch (Exception e) {
			logger.error(e);
			json_res = new JSONRPCResult(JSONRPCResult.CODE_ERR_PARSE, null,
					JSONRPCResult.MSG_ERR_PARSE);
		} finally {
			json_bridge = null;
		}
		return json_res;
	}
}
