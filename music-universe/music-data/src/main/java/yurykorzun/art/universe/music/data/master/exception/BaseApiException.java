package yurykorzun.art.universe.music.data.master.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for all API exceptions
 */
@Getter
public abstract class BaseApiException extends RuntimeException {
    
    private final HttpStatus status;
    
    protected BaseApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
    
    protected BaseApiException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }
}
