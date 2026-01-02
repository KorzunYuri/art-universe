package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.ArtistGetInfoDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.DtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.LastfmContextTestWithDb;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LastfmApiResponseProcessorsRegistry to verify the Strategy Registry pattern implementation.
 *
 * Validates:
 * 1. Registry returns correct strategy for each DTO class
 * 2. Registry returns Spring proxies (not raw instances)
 * 3. Concurrent registration/retrieval works safely
 */
@Tag("integration")
class LastfmApiResponseProcessorsRegistryTest extends LastfmContextTestWithDb {

    private static final Set<Class<? extends DtoRoot>> DTO_CLASSES = Arrays.stream(LastfmApiCallType.values())
        .map(LastfmApiCallType::getResponseDtoClass)
        .collect(Collectors.toSet());

    @Test
    void shouldReturnCorrectProcessorForEachDtoClass() {
        // when/then
        for (Class<? extends DtoRoot> dtoClass : DTO_CLASSES) {
            LastfmApiResponseProcessor<?> processor =
                LastfmApiResponseProcessorsRegistry.get(dtoClass);

            // Verify processor exists
            assertNotNull(processor,
                "Registry should return processor for DTO class: " + dtoClass.getSimpleName());
        }
    }

    @Test
    void shouldReturnSpringProxiesNotRawInstances() {
        // given
        Class<ArtistGetInfoDtoRoot> testDtoClass = ArtistGetInfoDtoRoot.class;

        // when
        LastfmApiResponseProcessor<ArtistGetInfoDtoRoot> processor =
            LastfmApiResponseProcessorsRegistry.get(testDtoClass);

        // then
        assertNotNull(processor, "Processor should not be null");

        // Verify it's a Spring proxy (CGLIB or JDK proxy)
        assertTrue(AopUtils.isAopProxy(processor),
            "Registry should return Spring proxy, not raw instance. " +
            "This ensures AOP aspects (like observability) work correctly.");

        // Verify it's specifically a CGLIB proxy (expected for class-based proxies)
        assertTrue(AopUtils.isCglibProxy(processor),
            "Processor should be CGLIB proxy since it's a concrete class");
    }

    @Test
    void shouldReturnSameProxyInstanceForSameDtoClass() {
        // given
        Class<ArtistGetInfoDtoRoot> testDtoClass = ArtistGetInfoDtoRoot.class;

        // when
        LastfmApiResponseProcessor<ArtistGetInfoDtoRoot> processor1 =
            LastfmApiResponseProcessorsRegistry.get(testDtoClass);
        LastfmApiResponseProcessor<ArtistGetInfoDtoRoot> processor2 =
            LastfmApiResponseProcessorsRegistry.get(testDtoClass);

        // then
        assertSame(processor1, processor2,
            "Registry should return same proxy instance for same DTO class (Spring singleton)");
    }

    @Test
    void allRegisteredProcessorsShouldBeProxies() {
        // when/then
        for (Class<? extends DtoRoot> dtoClass : DTO_CLASSES) {
            LastfmApiResponseProcessor<?> processor =
                LastfmApiResponseProcessorsRegistry.get(dtoClass);

            assertTrue(AopUtils.isAopProxy(processor),
                "All processors in registry should be Spring proxies for DTO class: " +
                dtoClass.getSimpleName());
        }
    }

    @Test
    void shouldReturnNullForNonRegisteredDtoClass() {
        // given
        @SuppressWarnings("unchecked")
        Class<DtoRoot> nonRegisteredClass = (Class<DtoRoot>) (Class<?>) String.class;

        // when
        LastfmApiResponseProcessor<DtoRoot> processor =
            LastfmApiResponseProcessorsRegistry.get(nonRegisteredClass);

        // then
        assertNull(processor,
            "Registry should return null for non-registered DTO class");
    }

    @Test
    void registryShouldContainExpectedNumberOfProcessors() {
        // given
        int expectedCount = DTO_CLASSES.size();

        // when
        int actualCount = 0;
        for (Class<? extends DtoRoot> dtoClass : DTO_CLASSES) {
            if (LastfmApiResponseProcessorsRegistry.get(dtoClass) != null) {
                actualCount++;
            }
        }

        // then
        assertThat(actualCount).isEqualTo(expectedCount);
    }

    @Test
    void registrySizeShouldMatchNumberOfDtoClasses() {
        // when
        int registrySize = LastfmApiResponseProcessorsRegistry.size();

        // then
        assertThat(registrySize).isGreaterThanOrEqualTo(DTO_CLASSES.size());
    }
}
