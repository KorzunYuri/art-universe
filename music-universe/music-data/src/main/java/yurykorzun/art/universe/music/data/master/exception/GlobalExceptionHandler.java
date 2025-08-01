package yurykorzun.art.universe.music.data.master.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for all API exceptions
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseApiException.class)
    public ResponseEntity<ErrorResponse> handleBaseApiException(BaseApiException ex) {
        return new ResponseEntity<>(
                new ErrorResponse(ex.getMessage()),
                ex.getStatus()
        );
    }
    
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
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String error = String.format("Parameter '%s' should be of type %s", 
                ex.getName(), ex.getRequiredType().getSimpleName());
                
        return new ResponseEntity<>(
                new ErrorResponse(error),
                HttpStatus.BAD_REQUEST
        );
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return new ResponseEntity<>(
                new ErrorResponse("An unexpected error occurred: " + ex.getMessage()),
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
