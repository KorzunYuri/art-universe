package yurykorzun.art.universe.music.data.raw.lastfm.common.exception;

/**
 * Base exception class for all application-specific exceptions.
 */
public abstract class ApplicationException extends RuntimeException {
    
    public ApplicationException(String message) {
        super(message);
    }
    
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
