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

import java.lang.reflect.Method;

import com.beetle.framework.web.controller.ControllerException;
import com.beetle.framework.web.controller.ControllerHelper;

/**
 * <pre>
 * 动作控制器，即可以用一个控制器处理多个页面提交的动作。
 * 每一个动作对应一个自定义的方法。方法定义满足以下原则：
 * 1--在符合java规范条件下，随便定义。 
 * 2--方法的输入参数只能是AjaxRequest，返回类型必须是AjaxResponse
 * 3--方法必须抛出ControllerException异常
 * 4--方法必须使用public修饰
 * eg:AjaxResponse xxxAction(AjaxRequest req)	throws ControllerException
 * 注意：在页面提交表单中，必须用'$action'关键字指定具体的方法名称。
 * 若'$action'不设置，则会执行defaultAction方法
 * </pre>
 */
public abstract class MultAjaxActionController implements ICommonAjax {

	final public AjaxResponse perform(AjaxRequest request)
			throws ControllerException {
		String actionName = request.getParameter("$action");
		if (actionName == null || actionName.length() == 0) {
			return defaultAction(request);
		}
		Method method = ControllerHelper.getActionMethod(
				request.getControllerName(), actionName, this,
				AjaxRequest.class);
		try {

			AjaxResponse view = (AjaxResponse) method.invoke(this,
					new Object[] { request });
			return view;
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}

	/**
	 * 默认执行动作（若$action没有设置，则会执行此方法）
	 * 
	 * @param request
	 * @return
	 * @throws ControllerException
	 */
	public abstract AjaxResponse defaultAction(AjaxRequest request)
			throws ControllerException;
}
