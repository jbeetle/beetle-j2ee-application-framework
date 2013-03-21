package com.beetle.framework.util.queue;

import org.junit.Test;

public class PersistQueueTest {

	@Test
	public void test() {
		PersistQueue pq = new PersistQueue(true, 10, "D:\\temp");
		for (int i = 0; i < 100; i++) {
			pq.push(i);
			// System.out.println(pq.pop());
		}
		for (int i = 0; i < 101; i++) {
			// pq.push(i);
			System.out.println(pq.pop());
		}
		System.out.println(pq.size());
		pq.close();
	}

}
