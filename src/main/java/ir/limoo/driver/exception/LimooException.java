package ir.limoo.driver.exception;

public class LimooException extends Exception {

	private static final long serialVersionUID = 1L;

	public LimooException() {

	}

	public LimooException(String str) {
		super(str);
	}

	public LimooException(Throwable t) {
		super(t);
	}
}
