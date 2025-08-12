package yurykorzun.art.universe.common.exception;

/**
 * A wrapper around jakarta {@link jakarta.persistence.EntityNotFoundException} with a convenient constructor.
 */
public class EntityNotFoundException extends jakarta.persistence.EntityNotFoundException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String entityType, long id) {
        this(String.format("%s not found with id: %d", entityType, id));
    }
}
