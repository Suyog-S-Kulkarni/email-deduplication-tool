package emailtool.exception;

public class InvalidExcelException extends RuntimeException {

    public InvalidExcelException(String message) {
        super(message);
    }

    public InvalidExcelException(String message, Throwable cause) {
        super(message, cause);
    }
}
