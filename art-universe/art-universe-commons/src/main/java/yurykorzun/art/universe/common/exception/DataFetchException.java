package yurykorzun.art.universe.common.exception;

/**
 * Exception thrown when there's an error fetching data from the database or external API.
 */
public class DataFetchException extends ExposedException {
    
    public DataFetchException(String message) {
        super(message);
    }
    
}
