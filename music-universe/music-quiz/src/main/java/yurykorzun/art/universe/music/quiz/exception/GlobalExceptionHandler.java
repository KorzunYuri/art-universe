package yurykorzun.art.universe.music.quiz.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all API exceptions
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle custom API exceptions
     */
    @ExceptionHandler(BaseApiException.class)
    public ResponseEntity<ErrorResponse> handleBaseApiException(BaseApiException ex) {
        // Log the full exception with stack trace for debugging
        if (ex.getCause() != null) {
            log.error("API exception occurred: {}", ex.getMessage(), ex);
        }
        
        return new ResponseEntity<>(
                new ErrorResponse(ex.getMessage()),
                ex.getStatus()
        );
    }
    
    /**
     * Handle validation exceptions from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        return new ResponseEntity<>(
                new ErrorResponse("Validation failed", errors),
                HttpStatus.BAD_REQUEST
        );
    }
    
    /**
     * Handle constraint violations
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> 
            errors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );
        
        return new ResponseEntity<>(
                new ErrorResponse("Validation failed", errors),
                HttpStatus.BAD_REQUEST
        );
    }
    
    /**
     * Handle type mismatch exceptions (e.g., passing a string where a number is expected)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String error = String.format("Parameter '%s' should be of type %s", 
                ex.getName(), ex.getRequiredType().getSimpleName());
                
        return new ResponseEntity<>(
                new ErrorResponse(error),
                HttpStatus.BAD_REQUEST
        );
    }
    
    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        String error = String.format("Required parameter '%s' of type %s is missing", 
                ex.getParameterName(), ex.getParameterType());
        
        return new ResponseEntity<>(
                new ErrorResponse(error),
                HttpStatus.BAD_REQUEST
        );
    }
    
    /**
     * Handle malformed JSON requests
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("Malformed JSON request", ex);
        return new ResponseEntity<>(
                new ErrorResponse("Malformed JSON request. Please check your request body."),
                HttpStatus.BAD_REQUEST
        );
    }
    
    /**
     * Handle JPA entity not found exceptions
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        log.error("Entity not found", ex);
        return new ResponseEntity<>(
                new ErrorResponse("Requested entity could not be found"),
                HttpStatus.NOT_FOUND
        );
    }
    
    /**
     * Handle empty result data access exceptions (e.g., when deleting a non-existent entity)
     */
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ErrorResponse> handleEmptyResultDataAccess(EmptyResultDataAccessException ex) {
        log.error("No data found for the requested operation", ex);
        return new ResponseEntity<>(
                new ErrorResponse("No data found for the requested operation"),
                HttpStatus.NOT_FOUND
        );
    }
    
    /**
     * Handle data integrity violations (e.g., unique constraint violations)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation", ex);
        return new ResponseEntity<>(
                new ErrorResponse("Data integrity violation. The operation could not be completed."),
                HttpStatus.CONFLICT
        );
    }
    
    /**
     * Handle general data access exceptions
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex) {
        log.error("Database access error", ex);
        return new ResponseEntity<>(
                new ErrorResponse("A database error occurred. Details can be found in server logs."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
    
    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Invalid argument provided", ex);
        return new ResponseEntity<>(
                new ErrorResponse("Invalid argument: " + ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Log the full exception with stack trace for debugging
        log.error("Unexpected error occurred", ex);
        
        // Return a generic message without exposing internal details
        return new ResponseEntity<>(
                new ErrorResponse("An unexpected error occurred. Details can be found in server logs."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
    
    /**
     * Standard error response format
     */
    public static class ErrorResponse {
        private final String message;
        private final Map<String, String> errors;
        
        public ErrorResponse(String message) {
            this.message = message;
            this.errors = null;
        }
        
        public ErrorResponse(String message, Map<String, String> errors) {
            this.message = message;
            this.errors = errors;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Map<String, String> getErrors() {
            return errors;
        }
    }
}
