package test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.beetle.framework.util.structure.DocumentTemplate;

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

	/*
	 * static { new Thread(new T()).start(); }
	 */
	/**
	 * @param args
	 */
	public static class II {
		private String face;
		private String imp;

		public String getFace() {
			return face;
		}

		public void setFace(String face) {
			this.face = face;
		}

		public String getImp() {
			return imp;
		}

		public void setImp(String imp) {
			this.imp = imp;
		}

		public II() {
			super();
		}

	}

	public static void main(String[] args) throws IOException {
		String a="demo.persistence.dAo.IExpUserDao";
		boolean f=a.toLowerCase().matches(".*\\.dao\\..*");
		System.out.println(f);
		System.out.println(a);
	}

	public static void main2(String[] args) throws IOException {
		DocumentTemplate dt = new DocumentTemplate("d:\\");
		List mylist = new ArrayList();
		for (int i = 0; i < 10; i++) {
			II ii = new II();
			ii.setFace("face" + i);
			ii.setImp("imp" + i);
			mylist.add(ii);
		}
		Map m = new HashMap();
		m.put("iilist", mylist);
		// String x = dt.process(m, "DAOConfig.ftl");
		// System.out.println(x);
		FileWriter fw = new FileWriter(new File("d:\\1.xml"));
		dt.process(m, "DAOConfig.ftl", fw);
		dt.clearCache();
	}

}
