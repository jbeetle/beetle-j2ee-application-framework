package example.thread;

import com.beetle.framework.util.thread.ThreadImp;

public class SimpleThread extends ThreadImp {

	public SimpleThread(String threadName, long interval) {
		super(threadName, interval);
	}

	@Override
	protected void routine() throws Throwable {
		System.out.println(System.currentTimeMillis());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new SimpleThread("SimpleThread", 3000l).start();
	}

}
