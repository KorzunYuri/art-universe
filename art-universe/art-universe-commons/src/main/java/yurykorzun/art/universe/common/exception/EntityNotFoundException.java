package yurykorzun.art.universe.common.exception;

/**
 * A custom alternative to {@link jakarta.persistence.EntityNotFoundException} with a convenient constructor.
 */
public class EntityNotFoundException extends ExposedException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String entityType, long id) {
        this(String.format("%s not found with id: %d", entityType, id));
    }
}
