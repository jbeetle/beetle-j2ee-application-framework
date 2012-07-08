package example.di;

import javax.inject.Inject;

public class Dog {
	@Inject
	public Dog(IEchoService es) {
		super();
		this.es = es;
	}

	private IEchoService es;

	public void echo(String words) {
		System.out.println("dog1[" + es.echo(words) + "]");
	}
}
