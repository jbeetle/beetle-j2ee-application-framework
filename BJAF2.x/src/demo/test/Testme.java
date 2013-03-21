package test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Testme {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// String a=AesEncrypt.encrypt("yhd@19760224");
		// System.out.println(a);
		try {
			System.out.println(InetAddress.getLocalHost());//10.79.9.64
			System.out.println(InetAddress.getByName("127.0.0.1"));
			System.out.println(InetAddress.getByName("0.0.0.0"));
			System.out.println(String.format("i am %s and my ege is %s", "Henry",30));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
