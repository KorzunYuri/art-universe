package yurykorzun.art.universe.music.data.raw.spotify.application.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.spotify.application.StagingIterationApplicator;

/**
 * AOP aspect that creates timing metrics for staging iteration application operations.
 *
 * <p>Instruments {@link StagingIterationApplicator#apply(yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration)}
 * to measure how long it takes to apply a single sealed staging iteration to the target tables.</p>
 *
 * <p>Note: The {@code apply} method handles errors internally and marks the iteration as FAILED
 * without re-throwing. The {@code status=error} tag is therefore only recorded when the method
 * itself throws unexpectedly (e.g. infrastructure failure before error handling kicks in).</p>
 *
 * @see StagingIterationApplicator
 */
@Aspect
@Component
@Slf4j
public class StagingApplicationObservabilityAspect {

    private final MeterRegistry meterRegistry;

    public StagingApplicationObservabilityAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Intercepts all executions of {@link StagingIterationApplicator#apply} to record timing metrics.
     *
     * <p>Extracted tags:</p>
     * <ul>
     *   <li>status: success or error</li>
     * </ul>
     *
     * @param joinPoint the join point providing access to the method execution
     * @return the result of the original method execution
     * @throws Throwable if the original method throws an exception
     */
    @Around("execution(* yurykorzun.art.universe.music.data.raw.spotify.application.StagingIterationApplicator.apply(..))")
    public Object timeApplication(ProceedingJoinPoint joinPoint) throws Throwable {

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = joinPoint.proceed();
            sample.stop(createTimer("success"));
            log.debug("Staging iteration application completed");
            return result;
        } catch (Throwable e) {
            sample.stop(createTimer("error"));
            log.error("Staging iteration application failed unexpectedly", e);
            throw e;
        }
    }

    /**
     * Creates a Timer with appropriate tags for the staging application metric.
     *
     * @param status the execution status (success or error)
     * @return a configured Timer instance
     */
    private Timer createTimer(String status) {
        return Timer.builder("music.data.raw.spotify.staging.apply")
                .description("Spotify staging iteration application duration")
                .tag("status", status)
                .register(meterRegistry);
    }
}
