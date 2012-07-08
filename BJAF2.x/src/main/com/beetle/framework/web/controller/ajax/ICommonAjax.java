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

import com.beetle.framework.web.controller.ControllerException;

public interface ICommonAjax {
  /**
   * ajax控制逻辑执行方法，AjaxMainServlet会根据请求的名称来找到此接口的实现类，
   * 并执行此方法完成任务
   *
   * @param AjaxRequest 浏览器客户端提交的参数对象，参照传统的Http Request设计。提供一些方法便于获取参数。
   * @return AjaxResponse 返回一个AjaxResponse结果响应的数据对象，其数据格式为json。因为ajax是后台刷新，也
   *                      就是说返回的视图就是它自己本身。
   * @throws ControllerException
   */
  AjaxResponse perform(AjaxRequest request) throws ControllerException;
}
