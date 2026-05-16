package pl.kamjer.shoppinglistservice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Optional recovery when a versioned migration file was edited after it had already been applied:
 * Flyway fails validation with a checksum mismatch. {@code repair()} updates stored checksums to match
 * the files on disk. Use only after you confirm the SQL changes are correct; then disable the flag again.
 */
@Configuration
public class FlywayConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.flyway.repair-before-migrate", havingValue = "true")
    public FlywayMigrationStrategy flywayRepairThenMigrate() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
