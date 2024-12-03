package pl.kamjer.shoppinglistservice.config.components;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.kamjer.shoppinglistservice.repository.ShoppingItemRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@AllArgsConstructor
@Log4j2
public class ScheduledJob {

    private ShoppingItemRepository shoppingItemRepository;

    @Scheduled(cron = "0 0 12 * * ?")
    public void deleteOldData() {
        log.info("Running scheduled job: {}, : Deleting old data", LocalTime.now());
        shoppingItemRepository.deleteBySavedTimeBeforeAndBoughtIsTrue(LocalDateTime.now().minusMonths(1));
    }
}
