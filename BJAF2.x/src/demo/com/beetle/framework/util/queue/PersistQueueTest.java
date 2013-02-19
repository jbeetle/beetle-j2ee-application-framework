package com.beetle.framework.util.queue;

import org.junit.Test;

public class PersistQueueTest {

	@Test
	public void test() {
		PersistQueue pq = new PersistQueue(false,10,"D:\\temp");
		for(int i=0;i<100000;i++){
			pq.push(i);
		}
		System.out.println(pq.size());
		pq.close();
	}

}
