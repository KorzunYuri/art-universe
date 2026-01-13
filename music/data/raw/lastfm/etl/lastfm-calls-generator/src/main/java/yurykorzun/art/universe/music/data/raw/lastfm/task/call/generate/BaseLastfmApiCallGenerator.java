package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate;

import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;

/**
 * Base class for LastFM API call generators.
 *
 * <p>Generators self-register in the registry during construction by registering their
 * class type (not instance). This allows the registry to retrieve Spring-managed proxies
 * via ApplicationContext, ensuring AOP aspects (like timing metrics) work correctly.</p>
 *
 * @see LastfmApiCallGeneratorsRegistry
 */
public abstract class BaseLastfmApiCallGenerator {

    /**
     * Constructor that self-registers this generator class in the registry.
     * Registers the class type (not instance) to allow registry to retrieve
     * Spring-managed proxies with AOP applied.
     */
    protected BaseLastfmApiCallGenerator() {
        LastfmApiCallGeneratorsRegistry.register(getApiCallType(), this.getClass());
    }

    /**
     * Returns the API call type handled by this generator.
     */
    public abstract LastfmApiCallType getApiCallType();

    /**
     * Creates API calls for the specific API call type.
     */
    public abstract void createApiCalls();

}
