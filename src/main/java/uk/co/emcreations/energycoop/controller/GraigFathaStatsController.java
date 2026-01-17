package uk.co.emcreations.energycoop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.emcreations.energycoop.dto.VensysMeanData;
import uk.co.emcreations.energycoop.dto.VensysPerformanceData;
import uk.co.emcreations.energycoop.entity.PerformanceStatEntry;
import uk.co.emcreations.energycoop.model.Site;
import uk.co.emcreations.energycoop.security.HasGraigFathaAPIRead;
import uk.co.emcreations.energycoop.security.HasGraigFathaAPIStatsAdvanced;
import uk.co.emcreations.energycoop.service.GraigFathaStatsService;
import uk.co.emcreations.energycoop.util.EntityHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/graigFatha/stats")
@Tag(name = "Graig Fatha Statistics", description = "Statistics for the Graig Fatha wind farm")
public class GraigFathaStatsController {
    private final GraigFathaStatsService graigFathaStatsService;

    @HasGraigFathaAPIRead
    @GetMapping(name = "Current energy yield", value = "/energyYield")
    @Operation(summary = "Current energy yield", description = "Returns today's current energy yield")
    public VensysMeanData getEnergyYield() {
        return graigFathaStatsService.getMeanEnergyYield().orElse(VensysMeanData.builder().build());
    }

    @HasGraigFathaAPIRead
    @GetMapping(name = "Yesterday's performance", value = "/yesterdayPerformance")
    @Operation(summary = "Yesterday's performance", description = "Returns yesterday's performance")
    public VensysPerformanceData getYesterdayPerformance() {
        return graigFathaStatsService.getYesterdayPerformance();
    }

    @HasGraigFathaAPIStatsAdvanced
    @GetMapping(name = "Log performance", value = "/logPerformance/{from}/{to}")
    @Operation(summary = "Log performance", description = "Log performance data between dates")
    public void logPerformance(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate from,
                                                @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") final LocalDate to) {
        graigFathaStatsService.logPerformance(from, to);
    }
}