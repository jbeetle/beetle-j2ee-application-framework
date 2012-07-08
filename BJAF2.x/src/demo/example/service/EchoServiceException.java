package example.service;

public class EchoServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String errFlag;

	public EchoServiceException() {
		super();
	}

	public String getErrFlag() {
		return errFlag;
	}

	public EchoServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public EchoServiceException(String message, String errFlag) {
		super(message);
		this.errFlag = errFlag;
	}

	public EchoServiceException(Throwable cause) {
		super(cause);
	}

}
