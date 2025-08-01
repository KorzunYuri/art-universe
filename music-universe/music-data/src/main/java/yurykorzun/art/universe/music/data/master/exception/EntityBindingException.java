package yurykorzun.art.universe.music.data.master.exception;

import org.springframework.http.HttpStatus;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.EntityType;

/**
 * Exception thrown when there's an error binding external entities to internal ones
 */
public class EntityBindingException extends BaseApiException {
    
    public EntityBindingException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
    
    public EntityBindingException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST);
    }
    
    public EntityBindingException(DataSource dataSource, EntityType entityType, Long externalId) {
        super(String.format("Failed to bind %s entity with external ID %d from %s", 
                entityType, externalId, dataSource), HttpStatus.BAD_REQUEST);
    }
    
    public EntityBindingException(DataSource dataSource, EntityType sourceType, Long sourceId, 
                                 EntityType targetType, Long targetId) {
        super(String.format("Failed to bind relation between %s (ID: %d) and %s (ID: %d) from %s", 
                sourceType, sourceId, targetType, targetId, dataSource), HttpStatus.BAD_REQUEST);
    }
}
