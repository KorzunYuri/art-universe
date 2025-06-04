package yurykorzun.art.universe.music.data.raw.lastfm.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbMaintenanceCoordinator;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@RequiredArgsConstructor
public class CoordinatingTaskScheduler implements TaskScheduler {

    private final TaskScheduler delegate;

    private final DbMaintenanceCoordinator coord;

    private Runnable wrap(Runnable task) {
        return () -> {
            log.info("coordinating task {}", task);
            coord.executeIfAllowed(task);
        };
    }

    @Override public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
        return delegate.schedule(wrap(task), trigger);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
        return delegate.schedule(wrap(task), startTime);
    }

    @Override public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
        return delegate.schedule(wrap(task), startTime);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
        return delegate.scheduleAtFixedRate(wrap(task), startTime, period);
    }

    @Override public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
        return delegate.scheduleAtFixedRate(wrap(task), startTime, period);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
        return delegate.scheduleAtFixedRate(wrap(task), period);
    }

    @Override public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
        return delegate.scheduleAtFixedRate(wrap(task), period);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
        return delegate.scheduleWithFixedDelay(wrap(task), startTime, delay);
    }

    @Override public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long delay) {
        return delegate.scheduleWithFixedDelay(wrap(task), startTime, delay);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
        return delegate.scheduleWithFixedDelay(wrap(task), delay);
    }

    @Override public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay) {
        return delegate.scheduleWithFixedDelay(wrap(task), delay);
    }
}
