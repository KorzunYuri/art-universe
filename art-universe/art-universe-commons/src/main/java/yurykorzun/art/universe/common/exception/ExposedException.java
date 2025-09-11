package yurykorzun.art.universe.common.exception;

/**
 * Superclass for controlled exceptions. By default, error message will be exposed in response body.
 */
public class ExposedException extends RuntimeException {

    public ExposedException(String message) {
        super(message);
    }

}
