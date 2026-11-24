package ksc.go.tz.dashboard.services;

import afriUtils.responses.AfriException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Dashboards are computed entirely in PostgreSQL by {@code ksc_admin_dashboard} and {@code ksc_client_dashboard}
 * (see {@code src/main/resources/db/functions}); this service calls them and returns their JSON unchanged.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    /** Longest period the admin dashboard accepts, to keep the query cheap. */
    private static final long MAX_PERIOD_DAYS = 366;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public JsonNode adminDashboard(LocalDate from, LocalDate to) {
        LocalDate[] period = period(from, to);
        return call("ksc_admin_dashboard", "SELECT ksc_admin_dashboard(?, ?)::text", Date.valueOf(period[0]), Date.valueOf(period[1]));
    }

    public JsonNode staffDashboard(String staffId) {
        return call("ksc_staff_dashboard", "SELECT ksc_staff_dashboard(?)::text", required(staffId, "Staff member"));
    }

    public JsonNode supervisorDashboard(String supervisorId, LocalDate from, LocalDate to) {
        String id = required(supervisorId, "Supervisor");
        LocalDate[] period = period(from, to);
        return call("ksc_supervisor_dashboard", "SELECT ksc_supervisor_dashboard(?, ?, ?)::text",
                id, Date.valueOf(period[0]), Date.valueOf(period[1]));
    }

    /** [from, to]: defaults to the last 30 days ending today; at most MAX_PERIOD_DAYS long. */
    private static LocalDate[] period(LocalDate from, LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusDays(29);
        if (start.isAfter(end)) {
            throw new AfriException("'from' must be on or before 'to'");
        }
        if (ChronoUnit.DAYS.between(start, end) >= MAX_PERIOD_DAYS) {
            throw new AfriException("The period can be at most " + MAX_PERIOD_DAYS + " days");
        }
        return new LocalDate[]{start, end};
    }

    private static String required(String id, String label) {
        if (id == null || id.isBlank()) {
            throw new AfriException(label + " is required");
        }
        return id.trim();
    }

    public JsonNode clientDashboard(String clientId) {
        return call("ksc_client_dashboard", "SELECT ksc_client_dashboard(?)::text", required(clientId, "Client"));
    }

    private JsonNode call(String function, String sql, Object... args) {
        String json;
        try {
            json = jdbcTemplate.queryForObject(sql, String.class, args);
        } catch (DataAccessException e) {
            log.error("[DASHBOARD] {} failed: {}", function, e.getMostSpecificCause().getMessage());
            throw new AfriException("Dashboard is unavailable: database function " + function
                    + " failed or is not installed (" + e.getMostSpecificCause().getMessage() + ")");
        }
        try {
            // Keep money amounts exact (e.g. 210000.00) instead of turning them into doubles.
            return objectMapper.reader()
                    .with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                    .without(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES)
                    .readTree(json);
        } catch (Exception e) {
            throw new AfriException("Dashboard returned unreadable data from " + function);
        }
    }
}
