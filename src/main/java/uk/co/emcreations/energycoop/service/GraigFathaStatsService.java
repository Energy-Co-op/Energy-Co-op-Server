package uk.co.emcreations.energycoop.service;

import uk.co.emcreations.energycoop.dto.VensysMeanData;
import uk.co.emcreations.energycoop.dto.VensysPerformanceData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface GraigFathaStatsService {
    Optional<VensysMeanData> getMeanEnergyYield();
    VensysPerformanceData getYesterdayPerformance();
    Optional<VensysPerformanceData> getPerformance(LocalDateTime from, LocalDateTime to);
    void logPerformance(final LocalDate from, final LocalDate to);
}