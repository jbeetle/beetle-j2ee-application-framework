package test;

import com.beetle.framework.AppContext;
import com.beetle.framework.util.encrypt.AesEncrypt;

import example.di.IEchoService;

public class Testme {
	private static class T implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					System.out.println(Thread.currentThread().getId());
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

	static {
		new Thread(new T()).start();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String a = AesEncrypt.encrypt("760224");
		System.out.println(a);
		IEchoService eo=AppContext.getInstance().lookup(IEchoService.class);
		eo.echo("xx");
	}

}
