package com.beetle.framework.web.controller;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import com.beetle.framework.log.AppLogger;
import com.beetle.framework.web.common.CommonUtil;
import com.beetle.framework.web.view.ModelData;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

public abstract class WebServiceController extends AbnormalViewControlerImp {
	private static final AppLogger logger = AppLogger
			.getInstance(WebServiceController.class);

	@Override
	public void performX(WebInput webInput, OutputStream outputStream)
			throws ControllerException {
		this.setContentType("text/html");
		final ModelData md;
		String actionName = webInput.getParameter("$action");
		logger.debug("actionName:{}", actionName);
		logger.debug("controllerName:{}", webInput.getControllerName());
		if (actionName == null || actionName.length() == 0) {
			md = defaultAction(webInput);
		} else {
			Method method = ControllerHelper.getActionMethod(
					webInput.getControllerName(), actionName, this,
					WebInput.class);
			try {
				md = (ModelData) method.invoke(this, webInput);
			} catch (Exception e) {
				throw new ControllerException(e);
			}
		}
		if (md == null || outputStream == null) {
			throw new ControllerException(
					"service return is null,service must have data to return!");
		}
		String ecode = webInput.getCharacterEncoding();
		if (md.getDataType().equals(ModelData.DataType.XML)) {
			toXml(outputStream, md, ecode);
		} else if (md.getDataType().equals(ModelData.DataType.JSON)) {
			toJSON(outputStream, md);
		} else {
			dealOtherCase(webInput, outputStream, md, ecode);
		}
	}

	private void dealOtherCase(WebInput webInput, OutputStream outputStream,
			final ModelData md, String ecode) throws ControllerException {
		String selfDef = webInput.getParameter("$dataFormat", "");
		if (selfDef.equalsIgnoreCase("xml")) {
			toXml(outputStream, md, ecode);
		} else if (selfDef.equalsIgnoreCase("json")) {
			toJSON(outputStream, md);
		} else if (selfDef.equals("")) {
			String defaultFormat = (String) webInput.getRequest().getAttribute(
					CommonUtil.WEB_SERVICE_DATA_DEFAULT_FORMAT);
			if (defaultFormat.equalsIgnoreCase("xml")) {
				toXml(outputStream, md, ecode);
			} else if (defaultFormat.equalsIgnoreCase("json")) {
				toJSON(outputStream, md);
			} else {
				throw new ControllerException("not support [" + defaultFormat
						+ "] format yet!");
			}
		} else {
			throw new ControllerException("not support [" + selfDef
					+ "] format yet!");
		}
	}

	private void toJSON(OutputStream outputStream, final ModelData md) {
		XStream xtm = new XStream(new JettisonMappedXmlDriver());
		xtm.setMode(XStream.NO_REFERENCES);
		final String dd;
		if (md.isDataBeanFormatReturn()) {
			dd = xtm.toXML(md.getData());
		} else {
			dd = xtm.toXML(md.getDataMap());
		}
		logger.debug("toJSON:{}", dd);
		PrintWriter out = new PrintWriter(outputStream);
		try {
			out.print(dd);
		} finally {
			out.flush();
			out.close();
		}
	}

	private void toXml(OutputStream outputStream, final ModelData md,
			String ecode) {
		XStream xtm = new XStream();
		final String dd;
		if (md.isDataBeanFormatReturn()) {
			dd = xtm.toXML(md.getData());
		} else {
			dd = xtm.toXML(md.getDataMap());
		}
		logger.debug("toXML:{}", dd);
		PrintWriter out = new PrintWriter(outputStream);
		try {
			out.println("<?xml version=\"1.0\" encoding=\"" + ecode + "\"?>");
			out.print(dd);
		} finally {
			out.flush();
			out.close();
		}
	}

	/**
	 * 默认执行动作（若$action没有设置，则会执行此方法）
	 * 
	 * @param webInput
	 * @return ModelData--返回给客户端的数据对象
	 * @throws ControllerException
	 */
	public abstract ModelData defaultAction(WebInput webInput)
			throws ControllerException;
}
