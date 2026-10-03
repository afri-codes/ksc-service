package ksc.go.tz.dashboard.controllers;

import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.ApiResponseUtil;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ksc.go.tz.dashboard.services.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@Tag(name = "Dashboards", description = "Dashboards computed by PostgreSQL functions (ksc_admin_dashboard, ksc_client_dashboard, ksc_staff_dashboard, ksc_supervisor_dashboard)")
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DashboardController {
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;
    private final DashboardService dashboardService;

    @Operation(summary = "Super admin dashboard", description = "Company-wide figures: sites by service, leads, quotes, contracts "
            + "(active value, expiring in 30 days), subscriptions, jobs, invoices (invoiced, paid, outstanding, overdue), payments "
            + "(received in the period, by method, failed, in progress), revenue for the last 12 months and the 10 latest payments. "
            + "'from'/'to' (yyyy-MM-dd, inclusive) bound the in-period figures; default is the last 30 days, at most 366 days.")
    @Permission(name = "VIEW ADMIN DASHBOARD", code = "VIEW_ADMIN_DASHBOARD")
    @GetMapping("/dashboard/admin")
    public ApiResponseUtil.ApiResponseEntity<JsonNode> adminDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return apiResponseUtil.getResponse(dashboardService.adminDashboard(from, to));
    }

    @Operation(summary = "My client dashboard", description = "For the signed-in client: their sites, current contracts, invoices "
            + "(open, outstanding, overdue), recent payments and next scheduled jobs. A client's sites are those they registered.")
    @Permission(name = "VIEW CLIENT DASHBOARD", code = "VIEW_CLIENT_DASHBOARD")
    @GetMapping("/dashboard/client")
    public ApiResponseUtil.ApiResponseEntity<JsonNode> myClientDashboard(Authentication authentication) {
        return apiResponseUtil.getResponse(dashboardService.clientDashboard(authDetailsExtractor.getUserId(authentication).toString()));
    }

    @Operation(summary = "A client's dashboard (super admin)", description = "The client dashboard for any client, by client (user) ID.")
    @Permission(name = "VIEW ADMIN DASHBOARD", code = "VIEW_ADMIN_DASHBOARD")
    @GetMapping("/dashboard/clients/{clientId}")
    public ApiResponseUtil.ApiResponseEntity<JsonNode> clientDashboard(@PathVariable("clientId") String clientId) {
        return apiResponseUtil.getResponse(dashboardService.clientDashboard(clientId));
    }

    @Operation(summary = "My staff dashboard", description = "For the signed-in staff member: their crews, today's and upcoming jobs "
            + "(jobs of crews they belong to), current clock-in, attendance over the last 30 days (days, hours, out-of-site events, "
            + "missing clock-outs) and their ratings.")
    @Permission(name = "VIEW STAFF DASHBOARD", code = "VIEW_STAFF_DASHBOARD")
    @GetMapping("/dashboard/staff")
    public ApiResponseUtil.ApiResponseEntity<JsonNode> myStaffDashboard(Authentication authentication) {
        return apiResponseUtil.getResponse(dashboardService.staffDashboard(authDetailsExtractor.getUserId(authentication).toString()));
    }

    @Operation(summary = "A staff member's dashboard (super admin)", description = "The staff dashboard for any staff member, by user ID.")
    @Permission(name = "VIEW ADMIN DASHBOARD", code = "VIEW_ADMIN_DASHBOARD")
    @GetMapping("/dashboard/staff/{staffId}")
    public ApiResponseUtil.ApiResponseEntity<JsonNode> staffDashboard(@PathVariable("staffId") String staffId) {
        return apiResponseUtil.getResponse(dashboardService.staffDashboard(staffId));
    }

    @Operation(summary = "My supervisor dashboard", description = "For the signed-in supervisor (crews.supervisorId): their crews and members, "
            + "jobs (today, late, upcoming, by status), who is on site now, attendance, per-staff performance, client feedback and open "
            + "disputes, and checklist completion. 'from'/'to' (yyyy-MM-dd, inclusive) bound the period figures; default last 30 days.")
    @Permission(name = "VIEW SUPERVISOR DASHBOARD", code = "VIEW_SUPERVISOR_DASHBOARD")
    @GetMapping("/dashboard/supervisor")
    public ApiResponseUtil.ApiResponseEntity<JsonNode> mySupervisorDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {
        return apiResponseUtil.getResponse(dashboardService.supervisorDashboard(
                authDetailsExtractor.getUserId(authentication).toString(), from, to));
    }

    @Operation(summary = "A supervisor's dashboard (super admin)", description = "The supervisor dashboard for any supervisor, by user ID.")
    @Permission(name = "VIEW ADMIN DASHBOARD", code = "VIEW_ADMIN_DASHBOARD")
    @GetMapping("/dashboard/supervisors/{supervisorId}")
    public ApiResponseUtil.ApiResponseEntity<JsonNode> supervisorDashboard(
            @PathVariable("supervisorId") String supervisorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return apiResponseUtil.getResponse(dashboardService.supervisorDashboard(supervisorId, from, to));
    }

}
