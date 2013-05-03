package example.aop;

import com.beetle.framework.resource.dic.aop.AopManager;

public class Client {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AopManager am = AopManager.getGlobalManager();
		am.bind("echo", new EchoServiceInerceptor());
		am.bind("printEcho", new EchoServiceInerceptor());
		EchoService service = am.retrieve(EchoService.class);
		String e = service.echo("hi,Henry");
		System.out.println(e);
		service.printEcho("hi,tom");

	}

}
