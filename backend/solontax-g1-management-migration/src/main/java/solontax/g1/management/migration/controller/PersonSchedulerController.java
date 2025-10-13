package solontax.g1.management.migration.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import solontax.g1.management.migration.service.PersonScheduler;

@RestController
@RequestMapping("/v1/person/scheduler")
@RequiredArgsConstructor
public class PersonSchedulerController {
    private final PersonScheduler personScheduler;

    @PostMapping("/start/upsert-person-event")
    public String startUpsertPersonEvent() {
        personScheduler.startUpsertPersonEventSchedulers();
        return "Upsert person event schedulers start";
    }

    @PostMapping("/stop/upsert-person-event")
    public String stopUpsertPersonEvent() {
        personScheduler.stopUpsertPersonEventSchedulers();
        return "Upsert person event schedulers stop";
    }

    @PostMapping("/start/consume-upsert-person-event")
    public String startConsumeUpsertPersonEvent() {
        personScheduler.startConsumeUpsertPersonEventSchedulers();
        return "Consume upsert person event schedulers start";
    }

    @PostMapping("/stop/consume-upsert-person-event")
    public String stopConsumeUpsertPersonEvent() {
        personScheduler.stopConsumeUpsertPersonEventSchedulers();
        return "Consume upsert person event schedulers start";
    }
}
