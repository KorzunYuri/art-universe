package yurykorzun.art.universe.music.data.raw.lastfm.common.exception;

/**
 * Exception thrown when there's an error updating data in the database.
 */
public class DataUpdateException extends ApplicationException {
    
    public DataUpdateException(String message) {
        super(message);
    }
    
    public DataUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
