package example.di;

import com.beetle.framework.util.pattern.di.DIContainer;
import com.beetle.framework.util.pattern.di.ReleBinder;

public class Client {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ReleBinder dc = new ReleBinder();
		// dc.bind(IEchoService.class, EchoServiceImp.class, true);
		dc.bind(IEchoService.class, EchoServiceProvider.class);
		
		DIContainer di = new DIContainer(dc);
		Dog dog = di.retrieve(Dog.class);
		dog.echo("hi,i am Henry");
		Dog2 dog2 = di.retrieve(Dog2.class);
		dog2.echo("hi,my dog");
		Dog3 dog3 = di.retrieve(Dog3.class);
		dog3.echo("hi,baby");
	}

	public static void main_binService(String[] args) {
		ReleBinder dc = new ReleBinder();
		dc.bind(IEchoService.class, EchoServiceImp.class, true);
		DIContainer di = new DIContainer(dc);
		Dog dog = di.retrieve(Dog.class);
		dog.echo("hi,i am Henry");
		Dog2 dog2 = di.retrieve(Dog2.class);
		dog2.echo("hi,my dog");
		Dog3 dog3 = di.retrieve(Dog3.class);
		dog3.echo("hi,baby");
	}

}
