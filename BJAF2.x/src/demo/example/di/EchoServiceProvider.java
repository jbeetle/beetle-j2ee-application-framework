package example.di;

import com.beetle.framework.util.pattern.di.ServiceProvider;

public class EchoServiceProvider extends ServiceProvider<IEchoService> {

	@Override
	public IEchoService get() {
		if (Math.random() > 0.5) {
			return new EchoServiceImp();
		} else {
			return new EchoServiceImp2();
		}
	}

}
