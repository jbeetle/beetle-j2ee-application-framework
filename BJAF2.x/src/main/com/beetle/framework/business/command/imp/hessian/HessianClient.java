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
package com.beetle.framework.business.command.imp.hessian;

import com.caucho.hessian.client.HessianProxyFactory;

import java.net.MalformedURLException;

/**
 * <p>
 * Title: J2EE框架核心工具包
 * 
 * </p>
 * 
 * <p>
 * Description:
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
 * @author 余浩东（yuhaodong@gmail.com）
 * 
 * @version 1.0
 */
public class HessianClient {
	public static Object getServiceObjectBackToClient(
			Class<ICmdService> interfaceApi, String serviceUrl)
			throws MalformedURLException {
		HessianProxyFactory factory = new HessianProxyFactory();
		return factory.create(interfaceApi, serviceUrl);
	}

	public static Object getServiceObjectBackToClient(
			Class<ICmdService> interfaceApi, String serviceUrl,
			String username, String password) throws MalformedURLException {
		HessianProxyFactory factory = new HessianProxyFactory();
		factory.setUser(username);
		factory.setPassword(password);
		return factory.create(interfaceApi, serviceUrl);
	}
}
