package solontax.g1.management.migration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "solontax.g1.management")
@EnableJpaRepositories(basePackages = "solontax.g1.management.dao.repository")
@EntityScan(basePackages = "solontax.g1.management.dao.outbound.entity")
public class SolontaxG1ManagementMigrationApplication {
    public static void main(String[] args) {
        SpringApplication.run(SolontaxG1ManagementMigrationApplication.class, args);
    }
}
