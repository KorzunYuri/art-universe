package yurykorzun.art.universe.music.data.raw.lastfm.common.exception;

/**
 * Exception thrown when there's an error related to external API calls.
 */
public class ApiCallException extends ApplicationException {
    
    public ApiCallException(String message) {
        super(message);
    }
    
    public ApiCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
