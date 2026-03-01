package io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.adapter;

import io.jeecloud.aidigitalsignage.digitalsignage.domain.SystemHealth;
import io.jeecloud.aidigitalsignage.digitalsignage.domain.SystemHealthRepository;
import io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.entity.SystemHealthEntity;
import io.jeecloud.aidigitalsignage.digitalsignage.infrastructure.persistence.jpa.repository.SystemHealthJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * System Health Repository Adapter (Infrastructure Layer)
 * 
 * Implements domain repository interface using JPA.
 * Translates between domain models and JPA entities.
 */
@Repository
@RequiredArgsConstructor
public class SystemHealthRepositoryAdapter implements SystemHealthRepository {

    private final SystemHealthJpaRepository jpaRepository;

    @Override
    public Optional<SystemHealth> findCurrent() {
        return jpaRepository.findMostRecent().map(this::toDomain);
    }

    @Override
    public SystemHealth save(SystemHealth systemHealth) {
        SystemHealthEntity entity = toEntity(systemHealth);
        SystemHealthEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private SystemHealth toDomain(SystemHealthEntity entity) {
        return SystemHealth.builder()
                .status(entity.getStatus())
                .performance(SystemHealth.PerformanceMetrics.builder()
                        .currentFps(entity.getCurrentFps())
                        .avgFps(entity.getAvgFps())
                        .minFps(entity.getMinFps())
                        .maxFps(entity.getMaxFps())
                        .currentCpuTemp(entity.getCurrentCpuTemp())
                        .maxCpuTemp(entity.getMaxCpuTemp())
                        .cpuThreshold(entity.getCpuThreshold())
                        .build())
                .environment(SystemHealth.EnvironmentMetrics.builder()
                        .temperatureCelsius(entity.getTemperatureCelsius())
                        .humidityPercent(entity.getHumidityPercent())
                        .pressureHpa(entity.getPressureHpa())
                        .gasResistanceOhms(entity.getGasResistanceOhms())
                        .noiseDb(entity.getNoiseDb())
                        .build())
                .uptime(entity.getUptime())
                .build();
    }

    private SystemHealthEntity toEntity(SystemHealth domain) {
        SystemHealthEntity entity = new SystemHealthEntity();
        entity.setStatus(domain.getStatus());
        
        if (domain.getPerformance() != null) {
            entity.setCurrentFps(domain.getPerformance().getCurrentFps());
            entity.setAvgFps(domain.getPerformance().getAvgFps());
            entity.setMinFps(domain.getPerformance().getMinFps());
            entity.setMaxFps(domain.getPerformance().getMaxFps());
            entity.setCurrentCpuTemp(domain.getPerformance().getCurrentCpuTemp());
            entity.setMaxCpuTemp(domain.getPerformance().getMaxCpuTemp());
            entity.setCpuThreshold(domain.getPerformance().getCpuThreshold());
        }
        
        if (domain.getEnvironment() != null) {
            entity.setTemperatureCelsius(domain.getEnvironment().getTemperatureCelsius());
            entity.setHumidityPercent(domain.getEnvironment().getHumidityPercent());
            entity.setPressureHpa(domain.getEnvironment().getPressureHpa());
            entity.setGasResistanceOhms(domain.getEnvironment().getGasResistanceOhms());
            entity.setNoiseDb(domain.getEnvironment().getNoiseDb());
        }
        
        entity.setUptime(domain.getUptime());
        return entity;
    }
}
