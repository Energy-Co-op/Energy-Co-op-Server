package uk.co.emcreations.energycoop.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record VensysPerformanceData(String tid, LocalDateTime date,
                                    double availability,
                                    double energyYield,
                                    double powerAvg,
                                    double powerMax,
                                    double windAvg,
                                    double windMax,
                                    int valuesCount,
                                    int errorCount,
                                    double powerProductionTime,
                                    double lowWindTime,
                                    double errorTime, double serviceTime, double iceTime, double stormTime, double shadowTime,
                                    double twistTime, double gridFailureTime, double commFailureTime, double visitTime,
                                    double serverStopTime, double fireTime, double batMonitoringTime, double nightShutdownTime) {
}