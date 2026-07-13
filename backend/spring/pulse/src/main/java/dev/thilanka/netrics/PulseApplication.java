package dev.thilanka.netrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")    // 'auditorAwareRef' is the bean name of 'ApplicationAuditAware' class at 'BeansConfig' - Used to add user details for Auditing (Otherwise will record only created & modified dates at auditing)
@EnableCaching
@EnableAsync
@EnableScheduling
public class PulseApplication {

	public static void main(String[] args) {
		SpringApplication.run(PulseApplication.class, args);
	}

}
