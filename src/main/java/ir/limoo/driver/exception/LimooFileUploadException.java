package ir.limoo.driver.exception;

public class LimooFileUploadException extends LimooException {

    private static final long serialVersionUID = 1L;

    public LimooFileUploadException() {
    }

    public LimooFileUploadException(String str) {
        super(str);
    }

    public LimooFileUploadException(Throwable t) {
        super(t);
    }
}
