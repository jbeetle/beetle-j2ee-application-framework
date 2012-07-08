package example.service;

import java.util.List;

public interface IEchoService {
	String echo(String word);

	List<String> echoList(List<String> words);

	List<User> echoUserList(List<User> words);

	String echoWithExp(String word) throws EchoServiceException;

	void die(String word) throws EchoServiceException;
}
