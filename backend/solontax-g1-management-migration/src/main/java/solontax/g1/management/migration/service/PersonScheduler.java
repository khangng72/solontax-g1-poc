package solontax.g1.management.migration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonScheduler {
    private final TaskScheduler taskScheduler;
    private final PersonJobService personJobService;

    private ScheduledFuture<?> sendUpsertPersonEventFuture;
    private ScheduledFuture<?> consumeUpsertPersonEventFuture;

    public void startUpsertPersonEventSchedulers() {
        if (sendUpsertPersonEventFuture == null || sendUpsertPersonEventFuture.isCancelled()) {
            sendUpsertPersonEventFuture = taskScheduler.scheduleAtFixedRate(personJobService::sendUpsertPersonEvents, Duration.ofSeconds(2));
            log.info("sendUpsertPersonEvents scheduled every 2 seconds");
        }
    }

    public void stopUpsertPersonEventSchedulers() {
        if (sendUpsertPersonEventFuture != null) {
            sendUpsertPersonEventFuture.cancel(true);
            log.info("sendUpsertPersonEvent stopped");
        }
    }

    public void startConsumeUpsertPersonEventSchedulers() {
        if (consumeUpsertPersonEventFuture == null || consumeUpsertPersonEventFuture.isCancelled()) {
            consumeUpsertPersonEventFuture = taskScheduler.scheduleAtFixedRate(personJobService::consumeUpsertPersonEvents, Duration.ofSeconds(7));
            log.info("Consume upsert person event scheduled every 5 second");
        }
    }

    public void stopConsumeUpsertPersonEventSchedulers() {
        if (consumeUpsertPersonEventFuture != null) {
            consumeUpsertPersonEventFuture.cancel(true);
            log.info("consume upsert person event scheduler stopped");
        }
    }

    public boolean isUpsertPersonEventScheduleRunning() {
        return (sendUpsertPersonEventFuture !=null && !sendUpsertPersonEventFuture.isCancelled());
    }
}
