package example.service;

import com.beetle.framework.business.service.server.ServiceServer;

public class TheServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ServiceServer ss = new ServiceServer(7602);
		ss.start();
	}

}
