<template>
  <div class="panel">
    <h3 class="panel-title">Environment Context</h3>
    
    <div v-if="!environment" class="panel-empty">
      <p>No environment data available yet</p>
      <small>Sensor readings will appear after ETL process runs</small>
    </div>
    
    <div v-else class="environment-grid">
      <!-- Temperature -->
      <div class="env-card">
        <div class="env-icon">🌡️</div>
        <div class="env-label">Temperature</div>
        <div class="env-value">{{ formatNumber(environment.temperatureCelsius) }}°C</div>
        <div class="env-desc">Ambient air temperature</div>
      </div>
      
      <!-- Humidity -->
      <div class="env-card">
        <div class="env-icon">💧</div>
        <div class="env-label">Humidity</div>
        <div class="env-value">{{ formatNumber(environment.humidityPercent) }}%</div>
        <div class="env-desc">Relative humidity</div>
      </div>
      
      <!-- Pressure -->
      <div class="env-card">
        <div class="env-icon">🌐</div>
        <div class="env-label">Pressure</div>
        <div class="env-value">{{ formatNumber(environment.pressureHpa) }} hPa</div>
        <div class="env-desc">Atmospheric pressure</div>
      </div>
      
      <!-- Noise Level -->
      <div class="env-card">
        <div class="env-icon">🔊</div>
        <div class="env-label">Noise Level</div>
        <div class="env-value">{{ formatNumber(environment.noiseDb) }} dB</div>
        <div class="env-desc" :class="noiseClass">{{ noiseDescription }}</div>
      </div>
      
      <!-- Air Quality (Gas Resistance) -->
      <div class="env-card">
        <div class="env-icon">🌬️</div>
        <div class="env-label">Air Quality</div>
        <div class="env-value">{{ formatGasResistance(environment.gasResistanceOhms) }}</div>
        <div class="env-desc">Gas resistance (Ω)</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  environment: {
    type: Object,
    default: null
  }
});

/**
 * Format number with fallback
 */
const formatNumber = (value) => {
  if (value === null || value === undefined) return 'N/A';
  return typeof value === 'number' ? value.toFixed(1) : value;
};

/**
 * Format gas resistance (large numbers)
 */
const formatGasResistance = (value) => {
  if (value === null || value === undefined) return 'N/A';
  
  if (value >= 1000) {
    return `${(value / 1000).toFixed(1)}k`;
  }
  
  return value.toFixed(0);
};

/**
 * Noise level description
 */
const noiseDescription = computed(() => {
  const noise = props.environment?.noiseDb;
  
  if (!noise) return 'Unknown';
  
  if (noise < 40) return 'Quiet';
  if (noise < 60) return 'Moderate';
  if (noise < 80) return 'Loud';
  return 'Very Loud';
});

/**
 * Noise level warning class
 */
const noiseClass = computed(() => {
  const noise = props.environment?.noiseDb;
  
  if (!noise) return '';
  
  if (noise >= 80) return 'warning-critical';
  if (noise >= 60) return 'warning-high';
  return '';
});
</script>

<style scoped>
.panel {
  background: white;
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  box-shadow: var(--shadow-md);
}

.panel-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 1rem;
}

.panel-empty {
  text-align: center;
  padding: 2rem;
  color: var(--text-secondary);
}

.panel-empty small {
  display: block;
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: var(--text-tertiary);
}

.environment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 1rem;
}

.env-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1.25rem;
  border-radius: var(--radius-lg);
  text-align: center;
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s ease;
}

.env-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.env-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.env-label {
  font-size: 0.875rem;
  font-weight: 500;
  opacity: 0.9;
  margin-bottom: 0.5rem;
}

.env-value {
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 0.25rem;
}

.env-desc {
  font-size: 0.75rem;
  opacity: 0.8;
}

.warning-high {
  color: #fbbf24;
  font-weight: 600;
}

.warning-critical {
  color: #fca5a5;
  font-weight: 600;
}
</style>
