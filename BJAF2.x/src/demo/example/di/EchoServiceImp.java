package example.di;

public class EchoServiceImp implements IEchoService {

	@Override
	public String echo(String words) {
		return "echo:" + words;
	}

}
