package yurykorzun.art.universe.common.exception;

/**
 * Exception thrown when there's an error updating data in the database.
 */
public class DataUpdateException extends ExposedException {
    
    public DataUpdateException(String message) {
        super(message);
    }

}
