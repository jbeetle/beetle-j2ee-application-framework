package example.di;

public class EchoServiceImp2 implements IEchoService {

	@Override
	public String echo(String words) {
		return "echo-2:" + words;
	}

}
