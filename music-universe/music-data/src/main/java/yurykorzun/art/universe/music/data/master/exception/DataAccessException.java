package yurykorzun.art.universe.music.data.master.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there's an error accessing data
 */
public class DataAccessException extends BaseApiException {
    
    public DataAccessException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    public DataAccessException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
