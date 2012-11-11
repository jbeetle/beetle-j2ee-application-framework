package test;

import com.beetle.framework.util.encrypt.AesEncrypt;

public class Testme {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String a=AesEncrypt.encrypt("yhd@19760224");
		System.out.println(a);

	}

}
