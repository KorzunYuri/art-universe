package yurykorzun.art.universe.music.data.master.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested entity is not found
 */
public class CustomEntityNotFoundException extends BaseApiException {
    
    public CustomEntityNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
    
    public CustomEntityNotFoundException(String entityType, Long id) {
        super(String.format("%s not found with id: %d", entityType, id), HttpStatus.NOT_FOUND);
    }
    
    public CustomEntityNotFoundException(String entityType, String identifier, String value) {
        super(String.format("%s not found with %s: %s", entityType, identifier, value), HttpStatus.NOT_FOUND);
    }
}
