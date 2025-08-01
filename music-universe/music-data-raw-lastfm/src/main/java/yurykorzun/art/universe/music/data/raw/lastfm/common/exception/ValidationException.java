package yurykorzun.art.universe.music.data.raw.lastfm.common.exception;

/**
 * Exception thrown when input validation fails.
 */
public class ValidationException extends ApplicationException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
