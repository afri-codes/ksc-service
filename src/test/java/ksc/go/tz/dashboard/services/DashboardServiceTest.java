package ksc.go.tz.dashboard.services;

import afriUtils.responses.AfriException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private JdbcTemplate jdbc;
    private DashboardService service;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        service = new DashboardService(jdbc, new ObjectMapper());
    }

    @Test
    void adminDefaultsToTheLast30DaysAndKeepsAmountsExact() {
        when(jdbc.queryForObject(anyString(), eq(String.class), any(), any()))
                .thenReturn("{\"invoices\":{\"outstanding\":210000.00}}");

        JsonNode result = service.adminDashboard(null, null);

        LocalDate today = LocalDate.now();
        verify(jdbc).queryForObject("SELECT ksc_admin_dashboard(?, ?)::text", String.class,
                Date.valueOf(today.minusDays(29)), Date.valueOf(today));
        assertEquals("210000.00", result.at("/invoices/outstanding").decimalValue().toPlainString());
    }

    @Test
    void adminRejectsReversedOrTooLongPeriods() {
        assertThrows(AfriException.class, () -> service.adminDashboard(LocalDate.of(2026, 10, 3), LocalDate.of(2026, 1, 1)));
        assertThrows(AfriException.class, () -> service.adminDashboard(LocalDate.of(2024, 1, 1), LocalDate.of(2026, 1, 1)));
    }

    @Test
    void clientDashboardIsForTheGivenClient() {
        when(jdbc.queryForObject(anyString(), eq(String.class), any())).thenReturn("{\"clientId\":\"client-A\"}");

        assertEquals("client-A", service.clientDashboard(" client-A ").get("clientId").asText());
        verify(jdbc).queryForObject("SELECT ksc_client_dashboard(?)::text", String.class, "client-A");
    }

    @Test
    void missingFunctionGivesAClearError() {
        when(jdbc.queryForObject(anyString(), eq(String.class), any()))
                .thenThrow(new DataAccessResourceFailureException("function ksc_client_dashboard(character varying) does not exist"));

        AfriException e = assertThrows(AfriException.class, () -> service.clientDashboard("client-A"));
        assertTrue(e.getMessage().contains("ksc_client_dashboard failed or is not installed"));
    }

    @Test
    void staffDashboardIsForTheGivenStaffMember() {
        when(jdbc.queryForObject(anyString(), eq(String.class), any())).thenReturn("{\"staffId\":\"s-1\"}");

        service.staffDashboard(" s-1 ");

        verify(jdbc).queryForObject("SELECT ksc_staff_dashboard(?)::text", String.class, "s-1");
        assertThrows(AfriException.class, () -> service.staffDashboard(" "));
    }

    @Test
    void supervisorDashboardPassesTheSupervisorAndPeriod() {
        when(jdbc.queryForObject(anyString(), eq(String.class), any(), any(), any())).thenReturn("{}");

        service.supervisorDashboard("sup-1", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        verify(jdbc).queryForObject("SELECT ksc_supervisor_dashboard(?, ?, ?)::text", String.class,
                "sup-1", Date.valueOf(LocalDate.of(2026, 9, 1)), Date.valueOf(LocalDate.of(2026, 9, 30)));
        assertThrows(AfriException.class, () -> service.supervisorDashboard("sup-1", LocalDate.of(2026, 9, 30), LocalDate.of(2026, 9, 1)));
    }
}
