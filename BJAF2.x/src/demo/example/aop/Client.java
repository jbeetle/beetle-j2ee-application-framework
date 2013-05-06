package example.aop;

import com.beetle.framework.resource.dic.DIContainer;

public class Client {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DIContainer di = DIContainer.getInstance();
		di.init();
		EchoService service = di.retrieve(EchoService.class);
		String e = service.echo("hi,Henry");
		System.out.println(e);
		service.printEcho("hi,tom");
		service.echo("hi,Henry");

	}

}
