package example.service;

import java.util.ArrayList;
import java.util.List;

import com.beetle.framework.resource.dic.def.ServiceTransaction;
import com.beetle.framework.util.OtherUtil;

public class EchoServiceImpl implements IEchoService {
	@Override
	public String echo(String word) {
		return "echo:{" + word + "}";
	}
 
	@Override
	public List<String> echoList(List<String> words) {
		List<String> ls = new ArrayList<String>();
		for (String w : words) {
			ls.add("echo:" + w);
		}
		dp();
		return ls;
	}

	private void dp() {
		try {
			Thread.sleep(OtherUtil.randomInt(100, 1000));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<User> echoUserList(List<User> words) {
		List<User> ls = new ArrayList<User>();
		for (User w : words) {
			w.setUsername("echo:" + w.getUsername());
			ls.add(w);
		}
		dp();
		return ls;
	}

	@ServiceTransaction
	@Override
	public String echoWithExp(String word) throws EchoServiceException {
		if (word.equals("err")) {
			throw new EchoServiceException("errrr:" + word, "xxx_Err");
		}
		return "echo:{" + word + "}";
	}

	@Override
	public void die(String word) throws EchoServiceException {
		while (true) {
			System.out.println(word);
		}
	}
}
