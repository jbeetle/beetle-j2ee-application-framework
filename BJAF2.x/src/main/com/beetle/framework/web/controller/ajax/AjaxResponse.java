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

import com.beetle.framework.AppRuntimeException;

import java.util.HashMap;

public class AjaxResponse extends HashMap<Object, Object> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int returnFlag = 0;

	private String returnMsg = "ok";

	private boolean breakFlag; // 是否中断标记

	public AjaxResponse() {
	}

	/**
	 * 按名称设置返回值（Object）
	 * 
	 * @param name
	 *            名称
	 * @param value
	 *            值,不允许为null
	 */
	public void setValue(String name, Object value) {
		if (value == null) {
			throw new AppRuntimeException("key[" + name
					+ "]'s value can't be null!please check it!");
		}
		this.put(name, value);
	}

	public int getReturnFlag() {
		return returnFlag;
	}

	public String getReturnMsg() {
		return returnMsg;
	}

	public boolean isBreakFlag() {
		return breakFlag;
	}

	/**
	 * 设置控制器执行状态标记
	 * 
	 * @param returnFlag
	 *            int
	 */
	public void setReturnFlag(int returnFlag) {
		this.returnFlag = returnFlag;
	}

	/**
	 * 设置控制器返回状态信息
	 * 
	 * @param returnMsg
	 *            String
	 */
	public void setReturnMsg(String returnMsg) {
		this.returnMsg = returnMsg;
	}

	/**
	 * 设置是否中断标记 此标记只在globalFrontCall或globalBackCall回调事件设置，此标记为true则，处理完此回调事件后
	 * 主流程立即退出
	 * 
	 * @param breakFlag
	 *            boolean
	 */
	public void setBreakFlag(boolean breakFlag) {
		this.setReturnFlag(-760224);
		this.setReturnMsg("访问被中断！");
		this.breakFlag = breakFlag;
	}
}
