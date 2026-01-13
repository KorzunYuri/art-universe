package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.etl.entity.ApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.BaseApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiResponse;

/**
 * AOP aspect that instruments all implementations of {@link BaseApiResponseProcessor}
 * to create timing metrics for API response processing operations.
 *
 * <p>This aspect automatically instruments the {@code process()} method in all
 * processor implementations without requiring modifications to individual classes.</p>
 *
 * <p>Uses @Timed (not @Observed) because processing operations are asynchronous
 * and independent from API call execution. The database is the only connector
 * between execution and parsing phases, with no trace context propagation.
 * Processors read JSON from database and write entities - all internal operations
 * already traced by Spring Data JPA.</p>
 *
 * <p>Note: This aspect is kept in the lastfm module initially (YAGNI principle).
 * When Spotify or other sources are added and show shared processing patterns,
 * this can be extracted to a common module.</p>
 *
 * @see BaseApiResponseProcessor
 */
@Aspect
@Component
@Slf4j
public class ApiResponseProcessorObservabilityAspect {

    private final MeterRegistry meterRegistry;

    public ApiResponseProcessorObservabilityAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Intercepts all executions of {@link BaseApiResponseProcessor#process(ApiResponse)} to record timing metrics.
     *
     * <p>Extracted tags:</p>
     * <ul>
     *   <li>api_call_type: From processor.getApiCallType().getName()</li>
     *   <li>status: success or error</li>
     * </ul>
     *
     * @param joinPoint the join point providing access to the method execution
     * @param processor the processor instance being executed
     * @param response the API response being processed
     * @return the result of the original method execution
     * @throws Throwable if the original method throws an exception
     */
    @Around("execution(* yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.BaseApiResponseProcessor.process(..)) && target(processor) && args(response)")
    public Object timeProcessing(
        ProceedingJoinPoint joinPoint,
        BaseApiResponseProcessor<?> processor,
        LastfmApiResponse response
    ) throws Throwable {

        String apiCallType = processor.getApiCallType().getName();

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = joinPoint.proceed();
            sample.stop(createTimer(apiCallType, "success"));
            log.debug("API response processing completed for type: {}", apiCallType);
            return result;
        } catch (Throwable e) {
            sample.stop(createTimer(apiCallType, "error"));
            log.error("API response processing failed for type: {}", apiCallType, e);
            throw e;
        }
    }

    /**
     * Creates a Timer with appropriate tags for the API response processing metric.
     *
     * @param apiCallType the type of API call being processed
     * @param status the execution status (success or error)
     * @return a configured Timer instance
     */
    private Timer createTimer(String apiCallType, String status) {
        return Timer.builder("music.data.raw.lastfm.api.response.process")
                .description("Lastfm API response processing duration: single")
                .tag("api_call_type", apiCallType)
                .tag("status", status)
                .register(meterRegistry);
    }
}
