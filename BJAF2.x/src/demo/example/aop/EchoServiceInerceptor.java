package example.aop;

import com.beetle.framework.resource.dic.aop.AopInterceptor;

public class EchoServiceInerceptor extends AopInterceptor {
	/*
	 * public Object invoke(MethodInvocation i) throws Throwable {
	 * System.out.println("method " + i.getMethod() + " is called on " +
	 * i.getThis() + " with args " + i.getArguments()); Object ret =
	 * i.proceed(); System.out.println("method " + i.getMethod() + " returns " +
	 * ret); return ret; }
	 */
	@Override
	protected void before(String methodName, Object[] args) throws Throwable {
		System.out.println("before:arg[" + print(args) + "]methodName["
				+ methodName + "]");
	}

	@Override
	protected void after(Object returnValue, String methodName, Object[] args)
			throws Throwable {
		System.out.println("after:args[" + print(args) + "]returnvalue["
				+ returnValue + "]methodName[" + methodName + "]");
	}

	private String print(Object[] args) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			sb.append(args[i].toString());
		}
		return sb.toString();
	}
}
