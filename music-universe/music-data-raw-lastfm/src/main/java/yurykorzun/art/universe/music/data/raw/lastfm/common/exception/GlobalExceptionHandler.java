package yurykorzun.art.universe.music.data.raw.lastfm.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for centralizing error handling across the application.
 * This prevents error stack traces from cluttering logs for expected business exceptions.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        log.info("Entity not found: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        log.info("Validation error: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(DataFetchException.class)
    public ResponseEntity<ErrorResponse> handleDataFetch(DataFetchException ex) {
        log.error("Data fetch error: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(DataUpdateException.class)
    public ResponseEntity<ErrorResponse> handleDataUpdate(DataUpdateException ex) {
        log.error("Data update error: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(ApiCallException.class)
    public ResponseEntity<ErrorResponse> handleApiCall(ApiCallException ex) {
        log.error("API call error: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(MaintenanceException.class)
    public ResponseEntity<ErrorResponse> handleMaintenance(MaintenanceException ex) {
        log.error("Maintenance error: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplication(ApplicationException ex) {
        log.error("Application error: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return new ResponseEntity<>(new ErrorResponse("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
