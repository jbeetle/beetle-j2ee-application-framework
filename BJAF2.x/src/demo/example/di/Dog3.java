package example.di;

import javax.inject.Inject;

public class Dog3 {
	@Inject
	private IEchoService es;

	public void echo(String words) {
		System.out.println("dog3[" + es.echo(words) + "]");
	}
}
