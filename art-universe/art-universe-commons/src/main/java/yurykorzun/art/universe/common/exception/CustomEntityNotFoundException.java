package yurykorzun.art.universe.common.exception;

/**
 * A custom alternative to {@link jakarta.persistence.EntityNotFoundException} with a convenient constructor.
 */
public class CustomEntityNotFoundException extends ExposedException {

    public CustomEntityNotFoundException(String message) {
        super(message);
    }

    public CustomEntityNotFoundException(String entityType, long id) {
        this(String.format("%s not found with id: %d", entityType, id));
    }
}
