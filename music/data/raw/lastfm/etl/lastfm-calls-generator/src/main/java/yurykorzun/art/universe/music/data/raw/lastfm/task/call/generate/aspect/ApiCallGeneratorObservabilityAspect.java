package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.BaseLastfmApiCallGenerator;

/**
 * AOP aspect that instruments all implementations of {@link BaseLastfmApiCallGenerator}
 * to create timing metrics for API call generation operations.
 *
 * <p>This aspect automatically instruments the {@code createApiCalls()} method in all
 * generator implementations without requiring modifications to individual classes.</p>
 *
 * <p>Uses @Timed (not @Observed) because generators only perform internal database
 * operations which are already traced by Spring Data JPA. No external HTTP calls
 * are made at this level.</p>
 *
 * @see BaseLastfmApiCallGenerator
 */
@Aspect
@Component
@Slf4j
public class ApiCallGeneratorObservabilityAspect {

    private final MeterRegistry meterRegistry;

    public ApiCallGeneratorObservabilityAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Intercepts all executions of {@link BaseLastfmApiCallGenerator#createApiCalls()} to record timing metrics.
     * <p>Extracted tags:</p>
     * <ul>
     *   <li>api_call_type: From generator.getApiCallType().getName()</li>
     *   <li>status: success or error</li>
     * </ul>
     *
     * @param joinPoint the join point providing access to the method execution
     * @return the result of the original method execution
     * @throws Throwable if the original method throws an exception
     */
    @Around("execution(* yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.BaseLastfmApiCallGenerator+.createApiCalls(..)) && target(generator)")
    public Object timeGeneration(ProceedingJoinPoint joinPoint, BaseLastfmApiCallGenerator generator) throws Throwable {

        String apiCallType = generator.getApiCallType().getName();

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = joinPoint.proceed();
            sample.stop(createTimer(apiCallType, "success"));
            log.debug("API call generation completed for type: {}", apiCallType);
            return result;
        } catch (Throwable e) {
            sample.stop(createTimer(apiCallType, "error"));
            log.error("API call generation failed for type: {}", apiCallType, e);
            throw e;
        }
    }

    /**
     * Creates a Timer with appropriate tags for the API call generation metric.
     *
     * @param apiCallType the type of API call being generated
     * @param status the execution status (success or error)
     * @return a configured Timer instance
     */
    private Timer createTimer(String apiCallType, String status) {
        return Timer.builder("music.data.raw.lastfm.api.call.generate")
                .description("API call generation duration")
                .tag("api_call_type", apiCallType)
                .tag("status", status)
                .register(meterRegistry);
    }
}
