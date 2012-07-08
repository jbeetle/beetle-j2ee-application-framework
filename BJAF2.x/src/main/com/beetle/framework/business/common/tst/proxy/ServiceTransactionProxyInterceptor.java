package com.beetle.framework.business.common.tst.proxy;

import com.beetle.framework.AppRuntimeException;
import com.beetle.framework.business.command.CommandException;
import com.beetle.framework.business.command.CommandExecutor;
import com.beetle.framework.business.command.CommandImp;
import com.beetle.framework.business.service.server.ServiceConfig;
import com.beetle.framework.business.service.server.ServiceConfig.ServiceDef;
import com.beetle.framework.business.service.server.ServiceConfig.ServiceDef.MethodEx;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServiceTransactionProxyInterceptor implements InvocationHandler {
	private final String interfacename;

	public ServiceTransactionProxyInterceptor(final String interfacename) {
		this.interfacename = interfacename;
	}

	private static class Cmd extends CommandImp {
		public Cmd() {
			super();
		}

		private transient Method method;
		private Object[] args;
		private Object impObj;
		private Object result;

		public Object getResult() {
			return result;
		}

		public void setImpObj(Object impObj) {
			this.impObj = impObj;
		}

		public void setMethod(Method method) {
			this.method = method;
		}

		public void setArgs(Object[] args) {
			this.args = args;
		}

		private static final long serialVersionUID = 1L;

		@Override
		public void process() throws CommandException {
			try {
				this.result = method.invoke(impObj, args);
			} catch (Exception e) {
				CommandException ce = new CommandException(e);
				if (e instanceof java.lang.reflect.InvocationTargetException) {
					ce.setPlus(((java.lang.reflect.InvocationTargetException) e)
							.getTargetException());
				}
				throw ce;
			}
		}

	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		ServiceDef sd = ServiceConfig.lookup(this.interfacename);
		if (sd == null) {
			throw new AppRuntimeException("not found service[" + interfacename
					+ "] define in Service.xml");
		}
		Object imp = sd.getServiceImpInstanceRef();
		if (imp == null) {
			throw new AppRuntimeException("create service imp instance err!");
		}
		final MethodEx mex = sd.getMethodEx(method.getName(),
				method.getParameterTypes());
		try {
			if (mex.isWithTransaction()) {
				if (mex.isWithSynchronized()) {
					synchronized (mex) {
						return dealWithTransaction(method, args, imp);
					}
				} else {
					return dealWithTransaction(method, args, imp);
				}
			} else {
				if (mex.isWithSynchronized()) {
					synchronized (mex) {
						return method.invoke(imp, args);
					}
				} else {
					return method.invoke(imp, args);
				}
			}
		} catch (Throwable t) {
			if (t instanceof InvocationTargetException) {
				InvocationTargetException tt = (InvocationTargetException) t;
				throw tt.getTargetException();
			} else {
				throw t;
			}
		}
	}

	public static Object dealWithTransaction(Method method, Object[] args,
			Object imp) throws Throwable {
		Cmd cmd = new Cmd();
		cmd.setArgs(args);
		cmd.setMethod(method);
		cmd.setImpObj(imp);
		cmd = (Cmd) CommandExecutor.executeWithTransaction(cmd,
				CommandExecutor.COMMON_EXECUTE);
		if (cmd.getReturnFlag() < 0) {
			if (cmd.getPlus() != null)
				throw (Throwable) cmd.getPlus();
			throw new AppRuntimeException(cmd.getReturnFlag(),
					cmd.getReturnMsg());
		}
		return cmd.getResult();
	}

}
