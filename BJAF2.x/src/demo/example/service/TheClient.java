package example.service;

import java.util.ArrayList;
import java.util.List;

import com.beetle.framework.business.service.ServiceProxyFactory;
import com.beetle.framework.util.thread.Counter;

public class TheClient {
	static final IEchoService echoService = ServiceProxyFactory.lookup(
			IEchoService.class, false);

	// static final IEchoService echoService = ServiceProxyFactory
	// .localLookup(IEchoService.class);
	public static void main(String[] args) {
		String x = echoService.echo("xxxxx");
		System.out.println(x);
	}

	public static void main2(String[] args) {
		try {
			echoService.echoWithExp("err");
		} catch (EchoServiceException e) {
			System.out.println(e.getErrFlag());
			e.printStackTrace();
		} finally {
			ServiceProxyFactory.clearAll();
		}
	}

	public static void mainbbb(String[] args) {
		for (int i = 0; i < 1000; i++) {
			List<User> ls = new ArrayList<User>();
			User u1 = new User();
			u1.setUsername("hen" + i);
			User u2 = new User();
			u2.setUsername("dog" + i);
			ls.add(u1);
			ls.add(u2);
			System.out.println(echoService.echoUserList(ls));
		}
		ServiceProxyFactory.clearAll();
	}

	public static void main222(String[] args) {
		final IEchoService echoService = ServiceProxyFactory
				.lookup(IEchoService.class);
		for (int i = 0; i < 1000; i++) {
			List<String> ls = new ArrayList<String>();
			ls.add("aaaaaaa" + i);
			ls.add("aaaadddaaa" + i);
			ls.add("aaaasdfdsfaaa" + i);
			ls.add("aaaafsdfsdfsdfaaa" + i);
			System.out.println(echoService.echoList(ls));
		}
		ServiceProxyFactory.clearAll();
	}

	public static void main55(String[] args) {
		final IEchoService echoService = ServiceProxyFactory
				.lookup(IEchoService.class);
		final Counter ct = new Counter();
		for (int i = 0; i < 100; i++) {
			ct.increase();
			System.out.println(echoService.echo("hi,henry["
					+ ct.getCurrentValue() + "]"));
		}
	}

	public static void main666(String[] args) {
		final IEchoService echoService = ServiceProxyFactory
				.lookup(IEchoService.class);
		final Counter ct = new Counter();
		for (int i = 0; i < 100; i++) {
			new Thread(new Runnable() {
				public void run() {
					// System.out.println(echoService.echo("hi,henry["
					// + ct.increaseAndGet() + "]"));
					// System.out.println(ct.increaseAndGet());
					final long xx = ct.increaseAndGet();
					List<String> ls = new ArrayList<String>();
					ls.add("aaaaaaa" + xx);
					ls.add("aaaadddaaa" + xx);
					ls.add("aaaasdfdsfaaa" + xx);
					ls.add("aaaafsdfsdfsdfaaa" + xx);
					System.out.println(echoService.echoList(ls));
				}
			}).start();
		}
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(30 * 1000);
						// ServiceProxyFactory.clearAll();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

}
