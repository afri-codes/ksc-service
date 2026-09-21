package ksc.go.tz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"ksc.go.tz","afriUtils","afriSecurity"})
@EnableJpaRepositories(basePackages = {
		"ksc.go.tz",
		"afriSecurity.permissions.repository",
		"afriSecurity.auditLogs.repositories"

})
@EntityScan(basePackages = {
		"ksc.go.tz",
		"afriSecurity"
})
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class KscApplication {

	public static void main(String[] args) {
		SpringApplication.run(KscApplication.class, args);
	}

}
