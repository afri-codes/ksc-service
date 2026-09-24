package ksc.go.tz.job.services;

import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.AfriException;
import jakarta.validation.Valid;
import ksc.go.tz.enums.TimeLogStatus;
import ksc.go.tz.job.dto.TimeLogDto;
import ksc.go.tz.job.dto.TimeLogResponseDto;
import ksc.go.tz.job.entities.Job;
import ksc.go.tz.job.entities.TimeLog;
import ksc.go.tz.job.repository.JobRepository;
import ksc.go.tz.job.repository.TimeLogRepository;
import ksc.go.tz.sitesAndAssests.entities.Sites;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TimeLogServiceImpl implements TimeLogService {
    private final JobRepository jobRepository;
    private final TimeLogRepository timeLogRepository;
    private final AuthDetailsExtractor authDetailsExtractor;

    @Override
    public TimeLogResponseDto addClockInJob(TimeLogDto timeLogRequest, Authentication authentication) {

        UUID userId = authDetailsExtractor.getUserId(authentication);

        UUID jobId;
        try {
            jobId = UUID.fromString(timeLogRequest.getJobId());
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid job ID");
        }

        Job job = jobRepository.findById(jobId).orElseThrow(() -> new AfriException("Job not found"));

        Sites site = job.getSite();

        if (site == null) {
            throw new AfriException("Job does not have a site assigned");
        }

        String userGeofenceZone = timeLogRequest.getGeofenceZone();
        String jobPlotCoordinates = site.getPlotCoordinates();

        if (userGeofenceZone == null || userGeofenceZone.trim().isEmpty()) {
            throw new AfriException("User geofence coordinates are required");
        }

        if (jobPlotCoordinates == null || jobPlotCoordinates.trim().isEmpty()) {
            throw new AfriException("Job site coordinates are not configured");
        }

        double[] userCoordinates = parseCoordinates(userGeofenceZone);

        double[] siteCoordinates = parseCoordinates(jobPlotCoordinates);

        double userLatitude = userCoordinates[0];
        double userLongitude = userCoordinates[1];

        double siteLatitude = siteCoordinates[0];
        double siteLongitude = siteCoordinates[1];

        double distanceInMeters = calculateDistanceInMeters(
                userLatitude,
                userLongitude,
                siteLatitude,
                siteLongitude
        );

        boolean outOfSite = distanceInMeters > 10.0;

        if(outOfSite) {
            throw new AfriException("You are out of the job site geofence zone. Please move closer to the site to clock in.");
        }

        TimeLog timeLog = new TimeLog();

        timeLog.setJob(job);
        timeLog.setStaffId(userId);
        timeLog.setClockIn(LocalDateTime.now());
        timeLog.setGeofenceZoneIn(userGeofenceZone);

        if (outOfSite) {
            timeLog.setClockInStatus(TimeLogStatus.OUT_OF_SITE);
        } else {
            timeLog.setClockInStatus(TimeLogStatus.IN_SITE);
        }

        timeLogRepository.save(timeLog);

        return new TimeLogResponseDto(timeLog);
    }

    @Override
    public TimeLogResponseDto addClockOutJob( TimeLogDto timeLogRequest, Authentication authentication) {

        UUID userId = authDetailsExtractor.getUserId(authentication);

        UUID jobId;
        try {
            jobId = UUID.fromString(timeLogRequest.getJobId());
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid job ID");
        }

        Job job = jobRepository.findById(jobId).orElseThrow(() -> new AfriException("Job not found"));

        Sites site = job.getSite();

        if (site == null) {
            throw new AfriException("Job does not have a site assigned");
        }

        String userGeofenceZone = timeLogRequest.getGeofenceZone();
        String jobPlotCoordinates = site.getPlotCoordinates();

        if (userGeofenceZone == null || userGeofenceZone.trim().isEmpty()) {
            throw new AfriException("User geofence coordinates are required");
        }

        if (jobPlotCoordinates == null || jobPlotCoordinates.trim().isEmpty()) {
            throw new AfriException("Job site coordinates are not configured");
        }

        double[] userCoordinates = parseCoordinates(userGeofenceZone);

        double[] siteCoordinates = parseCoordinates(jobPlotCoordinates);

        double userLatitude = userCoordinates[0];
        double userLongitude = userCoordinates[1];

        double siteLatitude = siteCoordinates[0];
        double siteLongitude = siteCoordinates[1];

        double distanceInMeters = calculateDistanceInMeters(
                userLatitude,
                userLongitude,
                siteLatitude,
                siteLongitude
        );

        boolean outOfSite = distanceInMeters > 10.0;
        if(outOfSite) {
            throw new AfriException("You are out of the job site geofence zone. Please move closer to the site to clock Out.");
        }

        TimeLog timeLog = new TimeLog();
        timeLog.setJob(job);
        timeLog.setStaffId(userId);
        timeLog.setClockOut(LocalDateTime.now());
        timeLog.setGeofenceZoneOut(timeLogRequest.getGeofenceZone());
        timeLog.setUpdatedBy(authDetailsExtractor.getUserId(authentication));
        if (outOfSite) {
            timeLog.setClockOutStatus(TimeLogStatus.OUT_OF_SITE);
        } else {
            timeLog.setClockOutStatus(TimeLogStatus.IN_SITE);
        }
        return new TimeLogResponseDto(timeLogRepository.save(timeLog));
    }

    @Override
    public TimeLogResponseDto getTimeLogsForJob(String jobId, Authentication authentication) {
        return null;
    }

    @Override
    public TimeLogResponseDto getTimeLogById(String timeLogId) {
        return null;
    }

    @Override
    public TimeLogResponseDto updateTimeLog(String timeLogId, @Valid TimeLogDto timeLogRequest, Authentication authentication) {
        Optional<TimeLog> optionalTimeLog = timeLogRepository.findById(UUID.fromString(timeLogId));
        if (optionalTimeLog.isEmpty()) {
            throw new AfriException("Time log not found");
        }
        TimeLog timeLog = optionalTimeLog.get();
        return null;
    }

    @Override
    public TimeLogResponseDto getAttendanceByStaffId(String staffId, Authentication authentication) {
        return null;
    }


    private double[] parseCoordinates(String coordinates) {

        try {

            String[] parts = coordinates.split(",");

            if (parts.length != 2) {
                throw new AfriException("Invalid coordinates. Expected format: latitude,longitude");
            }

            double latitude = Double.parseDouble(parts[0].trim());
            double longitude = Double.parseDouble(parts[1].trim());

            if (latitude < -90 || latitude > 90) {
                throw new AfriException("Invalid latitude");
            }

            if (longitude < -180 || longitude > 180) {
                throw new AfriException("Invalid longitude");
            }

            return new double[]{latitude, longitude
            };

        } catch (NumberFormatException e) {
            throw new AfriException("Invalid coordinates. Expected format: latitude,longitude"
            );
        }
    }

    private double calculateDistanceInMeters(double latitude1, double longitude1, double latitude2, double longitude2) {

        final double EARTH_RADIUS = 6_371_000;

        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);

        double deltaLat = Math.toRadians(latitude2 - latitude1);
        double deltaLon = Math.toRadians(longitude2 - longitude1);

        double a =
                Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                        + Math.cos(lat1)
                        * Math.cos(lat2)
                        * Math.sin(deltaLon / 2)
                        * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );

        return EARTH_RADIUS * c;
    }
}
