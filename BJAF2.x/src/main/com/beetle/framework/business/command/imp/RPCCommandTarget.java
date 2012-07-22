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
package com.beetle.framework.business.command.imp;

import java.net.MalformedURLException;

import com.beetle.framework.AppProperties;
import com.beetle.framework.business.command.CommandExecuteException;
import com.beetle.framework.business.command.CommandImp;
import com.beetle.framework.business.command.ICommandTarget;
import com.beetle.framework.business.command.imp.hessian.HessianClient;
import com.beetle.framework.business.command.imp.hessian.HessianCmdServiceException;
import com.beetle.framework.business.command.imp.hessian.ICmdService;

public class RPCCommandTarget implements ICommandTarget {
	private static ICommandTarget instance = new RPCCommandTarget();
	private String serviceName;

	private RPCCommandTarget() {
		serviceName = AppProperties.get("command_rpc_http_service_url");
	}

	public static ICommandTarget getInstance() {
		return instance;
	}

	/**
	 * executeCommand
	 * 
	 * @param command
	 *            CommandImp
	 * @return CommandImp
	 * @throws CommandExecuteException
	 * @todo Implement this com.beetle.framework.business.command.ICommandTarget
	 *       method
	 */
	public CommandImp executeCommand(CommandImp command)
			throws CommandExecuteException {
		ICmdService srv;
		try {
			srv = (ICmdService) HessianClient.getServiceObjectBackToClient(
					ICmdService.class, serviceName);
			Object obj = srv.perform(command,
					ICmdService.EXECUTE_WITHOUT_TRANSACTION);
			CommandImp cmd = (CommandImp) obj;
			return cmd;
		} catch (MalformedURLException ex1) {
			ex1.printStackTrace();
			throw new CommandExecuteException(ex1);
		} catch (HessianCmdServiceException ex) {
			ex.printStackTrace();
			throw new CommandExecuteException(ex);
		}
	}

	/**
	 * executeCommandNotTrans
	 * 
	 * @param command
	 *            CommandImp
	 * @return CommandImp
	 * @throws CommandExecuteException
	 * @todo Implement this com.beetle.framework.business.command.ICommandTarget
	 *       method
	 */
	public CommandImp executeCommandWithTransation(CommandImp command)
			throws CommandExecuteException {
		ICmdService srv;
		try {
			srv = (ICmdService) HessianClient.getServiceObjectBackToClient(
					ICmdService.class, serviceName);
			Object obj = srv.perform(command,
					ICmdService.EXECUTE_WITH_TRANSACTION);
			CommandImp cmd = (CommandImp) obj;
			return cmd;
		} catch (MalformedURLException ex1) {
			ex1.printStackTrace();
			throw new CommandExecuteException(ex1);
		} catch (HessianCmdServiceException ex) {
			ex.printStackTrace();
			throw new CommandExecuteException(ex);
		}
	}
}
