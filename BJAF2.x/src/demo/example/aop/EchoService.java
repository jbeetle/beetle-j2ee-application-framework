package example.aop;

import com.beetle.framework.resource.dic.aop.Aop;

public class EchoService {

	@Aop(id = "echo")
	public String echo(String word) {
		return "echo:" + word;
	}

	@Aop(id = "printEcho")
	public void printEcho(String word) {
		System.out.println("printEcho:" + word);
	}

	public void other() {
		System.out.println("OK");
	}
}
