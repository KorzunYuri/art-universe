package yurykorzun.art.universe.music.data.raw.lastfm.common.exception;

/**
 * Exception thrown when there's an error during LastFM maintenance operations.
 * This is module-specific exception for LastFM service maintenance, API limits, etc.
 */
public class MaintenanceException extends RuntimeException {
    
    public MaintenanceException(String message) {
        super(message);
    }
    
    public MaintenanceException(String message, Throwable cause) {
        super(message, cause);
    }
}
