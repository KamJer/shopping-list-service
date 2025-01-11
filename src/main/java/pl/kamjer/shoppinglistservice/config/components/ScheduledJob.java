package pl.kamjer.shoppinglistservice.config.components;

import jakarta.transaction.Transactional;
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
    @Transactional
    public void deleteOldData() {
        log.info("Running scheduled job: {}, : Deleting old data", LocalTime.now());
        shoppingItemRepository.findShoppingItemBeforeAndBoughtIsTrue(LocalDateTime.now().minusMonths(1))
                .forEach(shoppingItem -> {
                    shoppingItem.setDeleted(true);
                }
        );

    }
}
