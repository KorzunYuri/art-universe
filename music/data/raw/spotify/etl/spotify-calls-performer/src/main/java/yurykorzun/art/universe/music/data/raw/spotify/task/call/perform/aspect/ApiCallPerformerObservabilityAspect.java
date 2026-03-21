package yurykorzun.art.universe.music.data.raw.spotify.task.call.perform.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.task.call.perform.SpotifyApiCallExecutor;

/**
 * AOP aspect that creates timing metrics for API call execution operations.
 *
 * <p>Uses @Timed (not @Observed) as we are only interested in single call operation duration.</p>
 *
 * @see SpotifyApiCallExecutor
 */
@Aspect
@Component
@Slf4j
public class ApiCallPerformerObservabilityAspect {

    private final MeterRegistry meterRegistry;

    public ApiCallPerformerObservabilityAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Intercepts all executions of {@link SpotifyApiCallExecutor#execute(SpotifyApiCall)}
     * to record timing metrics.
     *
     * <p>Extracted tags:</p>
     * <ul>
     *   <li>api_call_type: From call.getType().getName()</li>
     *   <li>status: success or error</li>
     * </ul>
     *
     * @param joinPoint the join point providing access to the method execution
     * @param call the API call being executed
     * @return the result of the original method execution
     * @throws Throwable if the original method throws an exception
     */
    @Around("execution(* yurykorzun.art.universe.music.data.raw.spotify.task.call.perform.SpotifyApiCallExecutor+.execute(..)) && args(call)")
    public Object timeExecution(ProceedingJoinPoint joinPoint, SpotifyApiCall call) throws Throwable {

        String apiCallType = call.getType().getName();

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = joinPoint.proceed();
            sample.stop(createTimer(apiCallType, "success"));
            log.debug("API call execution completed for type: {}", apiCallType);
            return result;
        } catch (Throwable e) {
            sample.stop(createTimer(apiCallType, "error"));
            log.error("API call execution failed for type: {}", apiCallType, e);
            throw e;
        }
    }

    /**
     * Creates a Timer with appropriate tags for the API call execution metric.
     *
     * @param apiCallType the type of API call being executed
     * @param status the execution status (success or error)
     * @return a configured Timer instance
     */
    private Timer createTimer(String apiCallType, String status) {
        return Timer.builder("music.data.raw.spotify.api.call.perform")
                .description("Spotify API call execution duration: single")
                .tag("api_call_type", apiCallType)
                .tag("status", status)
                .register(meterRegistry);
    }
}
