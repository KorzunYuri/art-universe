package yurykorzun.art.universe.music.data.raw.lastfm.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import yurykorzun.art.universe.common.exception.ErrorResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.common.exception.MaintenanceException;

/**
 * Module-specific exception handler for LastFM raw data service.
 * This handler has high priority and handles only module-specific exceptions.
 * Common exceptions are handled by CommonGlobalExceptionHandler from commons module.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MaintenanceException.class)
    public ResponseEntity<ErrorResponse> handleMaintenance(MaintenanceException ex) {
        log.error("LastFM maintenance error: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ErrorResponse(ex.getMessage()), 
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}
