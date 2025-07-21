package yurykorzun.art.universe.music.data.raw.lastfm.common.exception;

/**
 * Exception thrown when a requested entity is not found.
 */
public class EntityNotFoundException extends ApplicationException {
    
    public EntityNotFoundException(String message) {
        super(message);
    }
    
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
