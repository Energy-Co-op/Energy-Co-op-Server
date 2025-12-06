package uk.co.emcreations.energycoop.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.emcreations.energycoop.dto.VensysMeanData;
import uk.co.emcreations.energycoop.dto.VensysMeanDataResponse;
import uk.co.emcreations.energycoop.dto.VensysPerformanceData;
import uk.co.emcreations.energycoop.dto.VensysPerformanceDataResponse;
import uk.co.emcreations.energycoop.model.Site;
import uk.co.emcreations.energycoop.service.AlertService;
import uk.co.emcreations.energycoop.service.GraigFathaStatsService;
import uk.co.emcreations.energycoop.sourceclient.VensysGraigFathaClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraigFathaStatsServiceImpl implements GraigFathaStatsService {
    private final VensysGraigFathaClient client;
    private final AlertService alertService;

    @Value("${alerts.thresholds.availability:75.0}")
    private double availabilityThreshold;
    @Value("${alerts.thresholds.failure-time:100.0}")
    private double failureTimeThreshold;

    @Override
    public Optional<VensysMeanData> getMeanEnergyYield() {
        log.info("getEnergyYield() called");

        VensysMeanDataResponse meanDataResponse = client.getMeanEnergyYield();
        Optional<VensysMeanData> meanDataOptional = Optional.ofNullable(meanDataResponse.data());

        if (meanDataOptional.isPresent()) {
            return meanDataOptional;
        } else {
            log.warn("No mean energy yield data available from client, falling back to current performance data.");

            Optional<VensysPerformanceData> performanceData = getCurrentPerformance();

            if (performanceData.isPresent()) {
                return Optional.of(VensysMeanData.builder()
                        .value(performanceData.get().energyYield())
                        .build());
            } else {
                return Optional.empty(); // We tried everything, return an empty optional
            }
        }
    }

    @Override
    public VensysPerformanceData getYesterdayPerformance() {
        log.info("getYesterdayPerformance() called");

        var from = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIDNIGHT);
        var to = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MAX);

        return getPerformance(from, to)
                .orElseGet(() -> {
                    log.warn("No performance data available for yesterday, returning empty data");
                    return VensysPerformanceData.builder().date(from).build();
                });
    }

    @Override
    public Optional<VensysPerformanceData> getPerformance(final LocalDateTime from, final LocalDateTime to) {
        log.info("getPerformance() called from: {}, to: {}", from, to);

        var fromTimestamp = from.toEpochSecond(ZoneOffset.UTC);
        var toTimestamp = to.toEpochSecond(ZoneOffset.UTC);

        VensysPerformanceDataResponse response = client.getPerformance(fromTimestamp, toTimestamp);
        validatePerformanceData(response);

        if (isInvalidResponse(response)) {
            log.warn("No performance data available for period {} to {}", from, to);
            return Optional.empty();
        }

        return Optional.of(response.data()[0]);
    }

    private Optional<VensysPerformanceData> getCurrentPerformance() {
        log.info("getCurrentPerformance() called");

        VensysPerformanceDataResponse response = client.getCurrentPerformance();
        //validatePerformanceData(response); // For now don't alert on current performance calls

        if (isInvalidResponse(response)) {
            log.warn("No current performance data available");
            return Optional.empty();
        }

        return Optional.of(response.data()[0]);
    }

    private boolean isInvalidResponse(VensysPerformanceDataResponse response) {
        return response == null || response.data() == null || response.data().length == 0 || response.data()[0] == null;
    }

    private void validatePerformanceData(final VensysPerformanceDataResponse response) {
        var alertMessage = new StringBuilder();

        if (response == null) {
            alertMessage.append("Performance response is null.\n");
        } else if (response.data() == null) {
            alertMessage.append("Performance data array is null.\n");
        } else if (response.data().length == 0) {
            alertMessage.append("Performance data array is empty.\n");
        } else if (response.data()[0] == null) {
            alertMessage.append("First performance data entry is null.\n");
        } else {
            VensysPerformanceData data = response.data()[0];
            if (availabilityThreshold >= data.availability()) {
                alertMessage.append("Availability (").append(data.availability()).append("%) less than threshold (")
                        .append(availabilityThreshold).append("%).\n");
            }

            if (failureTimeThreshold < data.fireTime()) {
                alertMessage.append("Fire time (").append(data.fireTime()).append("s) exceeds threshold (")
                        .append(failureTimeThreshold).append("s).\n");
            }

            if (failureTimeThreshold < data.commFailureTime()) {
                alertMessage.append("Comm failure time (").append(data.commFailureTime()).append("s) exceeds threshold (")
                        .append(failureTimeThreshold).append("s).\n");
            }

            if (failureTimeThreshold < data.gridFailureTime()) {
                alertMessage.append("Grid failure time (").append(data.gridFailureTime()).append("s) exceeds threshold (")
                        .append(failureTimeThreshold).append("s).\n");
            }

            if (failureTimeThreshold < data.errorTime()) {
                alertMessage.append("Error time (").append(data.errorTime()).append("s) exceeds threshold (")
                        .append(failureTimeThreshold).append("s).\n");
            }
        }

        if (!alertMessage.isEmpty()) {
            var timeStr = (response != null && response.from() != null && response.to() != null)
                    ? response.from() + " -> " + response.to() + "\n"
                    : "Unknown";

            alertMessage.insert(0, "(" + timeStr + "): ");

            alertService.sendAlert(Site.GRAIG_FATHA, alertMessage.toString());
        }
    }
}
