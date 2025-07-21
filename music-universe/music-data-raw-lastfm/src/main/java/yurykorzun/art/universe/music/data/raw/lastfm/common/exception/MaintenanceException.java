package yurykorzun.art.universe.music.data.raw.lastfm.common.exception;

/**
 * Exception thrown when there's an error during maintenance operations.
 */
public class MaintenanceException extends ApplicationException {
    
    public MaintenanceException(String message) {
        super(message);
    }
    
    public MaintenanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
