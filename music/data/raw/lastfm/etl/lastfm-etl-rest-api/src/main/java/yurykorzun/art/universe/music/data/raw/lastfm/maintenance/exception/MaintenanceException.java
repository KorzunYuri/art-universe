package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.exception;

import yurykorzun.art.universe.common.exception.ExposedException;

/**
 * Exception thrown when there's an error during LastFM maintenance operations.
 * This is module-specific exception for LastFM service maintenance, API limits, etc.
 */
public class MaintenanceException extends ExposedException {
    
    public MaintenanceException(String message) {
        super(message);
    }
}
