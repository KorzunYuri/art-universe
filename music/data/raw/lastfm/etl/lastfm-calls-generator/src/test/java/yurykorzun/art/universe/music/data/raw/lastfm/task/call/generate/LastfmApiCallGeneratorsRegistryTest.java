package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes.LastfmContextTestWithDb;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class LastfmApiCallGeneratorsRegistryTest extends LastfmContextTestWithDb {

    @Test
    void shouldReturnCorrectGeneratorForEachCallType() {
        // given
        LastfmApiCallType[] allCallTypes = LastfmApiCallType.values();

        // when/then
        for (LastfmApiCallType callType : allCallTypes) {
            BaseLastfmApiCallGenerator generator = LastfmApiCallGeneratorsRegistry.get(callType);

            // Verify generator exists
            assertNotNull(generator,
                "Registry should return generator for call type: " + callType);

            // Verify generator returns correct call type
            assertEquals(callType, generator.getApiCallType(),
                "Generator should return correct call type: " + callType);
        }
    }

    @Test
    void shouldReturnAllRegisteredGenerators() {
        // when
        Map<LastfmApiCallType, BaseLastfmApiCallGenerator> registry =
            LastfmApiCallGeneratorsRegistry.getRegistry();

        // then
        assertThat(registry).isNotNull();
        assertThat(registry).isNotEmpty();

        // Verify each generator has correct call type
        registry.forEach((callType, generator) -> {
            assertEquals(callType, generator.getApiCallType(),
                "Generator in registry should match its key");
        });
    }

    @Test
    void shouldReturnSpringProxiesNotRawInstances() {
        // given
        LastfmApiCallType testCallType = LastfmApiCallType.TAG_TOP_TAGS;

        // when
        BaseLastfmApiCallGenerator generator = LastfmApiCallGeneratorsRegistry.get(testCallType);

        // then
        assertNotNull(generator, "Generator should not be null");

        // Verify it's a Spring proxy (CGLIB or JDK proxy)
        assertTrue(AopUtils.isAopProxy(generator),
            "Registry should return Spring proxy, not raw instance. " +
            "This ensures AOP aspects (like observability) work correctly.");

        // Verify it's specifically a CGLIB proxy (expected for class-based proxies)
        assertTrue(AopUtils.isCglibProxy(generator),
            "Generator should be CGLIB proxy since it's a concrete class");
    }

    @Test
    void shouldReturnSameProxyInstanceForSameCallType() {
        // given
        LastfmApiCallType testCallType = LastfmApiCallType.TAG_TOP_TAGS;

        // when
        BaseLastfmApiCallGenerator generator1 = LastfmApiCallGeneratorsRegistry.get(testCallType);
        BaseLastfmApiCallGenerator generator2 = LastfmApiCallGeneratorsRegistry.get(testCallType);

        // then
        assertSame(generator1, generator2,
            "Registry should return same proxy instance for same call type (Spring singleton)");
    }

    @Test
    void allRegisteredGeneratorsShouldBeProxies() {
        // when
        Map<LastfmApiCallType, BaseLastfmApiCallGenerator> registry =
            LastfmApiCallGeneratorsRegistry.getRegistry();

        // then
        assertThat(registry).isNotEmpty();

        registry.forEach((callType, generator) -> {
            assertTrue(AopUtils.isAopProxy(generator),
                "All generators in registry should be Spring proxies for call type: " + callType);
        });
    }
}
