package example.di;

import javax.inject.Inject;

public class Dog2 {
	private IEchoService es;

	@Inject
	public void setEs(IEchoService es) {
		this.es = es;
	}

	public void echo(String words) {
		System.out.println("dog2["+es.echo(words)+"]");
	}
}
