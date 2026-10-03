package ksc.go.tz.dashboard.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Installs the dashboard database functions ({@code classpath:db/functions/*.sql}) at startup. Each file holds one
 * {@code CREATE OR REPLACE FUNCTION}, so running them every start is safe and keeps the database in step with the code.
 * A failure is logged and does not stop the application; the dashboard endpoints then report that the functions are missing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardFunctionInstaller {

    private static final String LOCATION = "classpath:db/functions/*.sql";

    private final JdbcTemplate jdbcTemplate;

    @Value("${ksc.dashboard.install-functions:true}")
    private boolean enabled;

    @EventListener(ApplicationReadyEvent.class)
    public void install() {
        if (!enabled) {
            log.info("[DASHBOARD] Function install disabled (ksc.dashboard.install-functions=false)");
            return;
        }
        try {
            Resource[] scripts = new PathMatchingResourcePatternResolver().getResources(LOCATION);
            Arrays.sort(scripts, Comparator.comparing(Resource::getFilename));
            for (Resource script : scripts) {
                try {
                    jdbcTemplate.execute(script.getContentAsString(StandardCharsets.UTF_8));
                    log.info("[DASHBOARD] Installed {}", script.getFilename());
                } catch (Exception e) {
                    log.error("[DASHBOARD] Could not install {}: {}", script.getFilename(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[DASHBOARD] Could not read dashboard function scripts: {}", e.getMessage());
        }
    }
}
