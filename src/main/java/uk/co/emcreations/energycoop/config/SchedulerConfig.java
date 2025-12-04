package uk.co.emcreations.energycoop.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import uk.co.emcreations.energycoop.dto.VensysMeanData;
import uk.co.emcreations.energycoop.dto.VensysPerformanceData;
import uk.co.emcreations.energycoop.entity.GenerationStatEntry;
import uk.co.emcreations.energycoop.entity.PerformanceStatEntry;
import uk.co.emcreations.energycoop.model.Site;
import uk.co.emcreations.energycoop.service.GraigFathaStatsService;
import uk.co.emcreations.energycoop.util.EntityHelper;

import java.util.Optional;

import static uk.co.emcreations.energycoop.model.Site.GRAIG_FATHA;

@Slf4j
@Configuration
@Transactional
@EnableScheduling
@RequiredArgsConstructor
@Profile("!dev")
public class SchedulerConfig {
    @PersistenceContext
    private final EntityManager entityManager;

    @Autowired
    private final GraigFathaStatsService graigFathaStatsService;

    @Scheduled(cron = "${scheduling.graig-fatha.schedule.energy-yield:15 */15 * * * *}")
    public void logEnergyYield() {
        log.info("logEnergyYield running..");

        Optional<VensysMeanData> energyYieldOpt = graigFathaStatsService.getMeanEnergyYield();

        if (energyYieldOpt.isPresent()) {
            GenerationStatEntry statEntry = EntityHelper.createGenerationStatEntry(energyYieldOpt.get(), GRAIG_FATHA);
            entityManager.persist(statEntry);

            log.info("Response = {}", energyYieldOpt.get());
        } else {
            log.warn("No energy yield data available to log.");
        }
    }

    @Scheduled(cron = "${scheduling.graig-fatha.schedule.performance:5 0 */6 * * *}")
    public void logPerformance() {
        log.info("logPerformance running..");

        VensysPerformanceData performanceData = graigFathaStatsService.getYesterdayPerformance();

        PerformanceStatEntry statEntry = EntityHelper.createPerformanceStatEntry(performanceData, Site.GRAIG_FATHA);
        entityManager.persist(statEntry);

        log.info("Response = {}", performanceData);
    }
}
