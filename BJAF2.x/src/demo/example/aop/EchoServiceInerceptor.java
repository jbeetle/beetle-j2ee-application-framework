package example.aop;

import java.lang.reflect.Method;

import com.beetle.framework.resource.dic.aop.AopInterceptor;

public class EchoServiceInerceptor extends AopInterceptor {

	private String print(Object[] args) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			sb.append(args[i].toString());
		}
		return sb.toString();
	}

	@Override
	protected void before(Method method, Object[] args) throws Throwable {
		System.out.println("before:arg[" + print(args) + "]methodName["
				+ method + "]");

	}

	@Override
	protected boolean interrupt() {
		// TODO Auto-generated method stub
		return super.interrupt();
	}

	@Override
	protected Object interruptResult(Object proxy, Method method, Object[] args)
			throws Throwable {
		// TODO Auto-generated method stub
		return super.interruptResult(proxy, method, args);
	}

	@Override
	protected void after(Object returnValue, Method method, Object[] args)
			throws Throwable {
		System.out.println("after:args[" + print(args) + "]returnvalue["
				+ returnValue + "]methodName[" + method + "]");

	}

}
