/*
 * BJAF - Beetle J2EE Application Framework
 * 甲壳虫J2EE企业应用开发框架
 * 版权所有2003-2015 余浩东 (www.beetlesoft.net)
 * 
 * 这是一个免费开源的软件，您必须在
 *<http://www.apache.org/licenses/LICENSE-2.0>
 *协议下合法使用、修改或重新发布。
 *
 * 感谢您使用、推广本框架，若有建议或问题，欢迎您和我联系。
 * 邮件： <yuhaodong@gmail.com/>.
 */
package com.beetle.framework.util.thread;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 支持高并发性能的计数器
 * 
 */
public class Counter {
	private final AtomicLong count;

	public Counter() {
		this.count = new AtomicLong(0);
	}

	public Counter(long initialValue) {
		this.count = new AtomicLong(initialValue);
	}

	public long increaseAndGet() {
		return count.incrementAndGet();
	}

	/**
	 * 获取当前计数器的值
	 * 
	 * 
	 * @return
	 */
	public long getCurrentValue() {
		return this.count.get();
	}

	/**
	 * 加1
	 */
	public void increase() {
		this.count.incrementAndGet();
	}

	/**
	 * 减1
	 */
	public void decrease() {
		this.count.decrementAndGet();
	}
}
