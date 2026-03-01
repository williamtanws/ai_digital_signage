<template>
  <div class="panel">
    <h3 class="panel-title">System Performance</h3>
    
    <div v-if="!systemHealth" class="panel-empty">
      <p>No system health data available yet</p>
      <small>Performance metrics will appear after ETL process runs</small>
    </div>
    
    <div v-else class="panel-content">
      <!-- System Status Badge -->
      <div class="status-badge" :class="`status-${systemHealth.status?.toLowerCase() || 'unknown'}`">
        {{ systemHealth.status || 'UNKNOWN' }}
      </div>
      
      <!-- Performance Metrics Grid -->
      <div class="metrics-grid">
        <!-- FPS Metrics -->
        <div class="metric-card">
          <div class="metric-label">Current FPS</div>
          <div class="metric-value">{{ formatNumber(systemHealth.performance?.currentFps) }}</div>
          <div class="metric-range">
            Avg: {{ formatNumber(systemHealth.performance?.avgFps) }} | 
            Range: {{ formatNumber(systemHealth.performance?.minFps) }}-{{ formatNumber(systemHealth.performance?.maxFps) }}
          </div>
          <div class="metric-target">Target: 5-10 FPS</div>
        </div>
        
        <!-- CPU Temperature -->
        <div class="metric-card">
          <div class="metric-label">CPU Temperature</div>
          <div class="metric-value">{{ formatNumber(systemHealth.performance?.currentCpuTemp) }}°C</div>
          <div class="metric-range">
            Max: {{ formatNumber(systemHealth.performance?.maxCpuTemp) }}°C
          </div>
          <div class="metric-target" :class="cpuTempClass">
            Threshold: {{ formatNumber(systemHealth.performance?.cpuThreshold) }}°C
          </div>
        </div>
        
        <!-- System Uptime -->
        <div class="metric-card">
          <div class="metric-label">System Uptime</div>
          <div class="metric-value">{{ formatUptime(systemHealth.uptime) }}</div>
          <div class="metric-range">Since last restart</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  systemHealth: {
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
 * Format uptime seconds to human-readable format
 */
const formatUptime = (seconds) => {
  if (!seconds) return 'N/A';
  
  // Parse string to number if needed
  const numSeconds = typeof seconds === 'string' ? parseInt(seconds, 10) : seconds;
  
  if (isNaN(numSeconds)) return 'N/A';
  
  const hours = Math.floor(numSeconds / 3600);
  const minutes = Math.floor((numSeconds % 3600) / 60);
  
  if (hours > 24) {
    const days = Math.floor(hours / 24);
    const remainingHours = hours % 24;
    return `${days}d ${remainingHours}h`;
  }
  
  return `${hours}h ${minutes}m`;
};

/**
 * CPU temperature warning class
 */
const cpuTempClass = computed(() => {
  const currentTemp = props.systemHealth?.performance?.currentCpuTemp;
  const threshold = props.systemHealth?.performance?.cpuThreshold || 70;
  
  if (!currentTemp) return '';
  
  if (currentTemp >= threshold) return 'warning-critical';
  if (currentTemp >= threshold - 5) return 'warning-high';
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

.panel-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.status-badge {
  display: inline-block;
  padding: 0.375rem 0.75rem;
  border-radius: var(--radius-full);
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.status-healthy {
  background: #d1fae5;
  color: #065f46;
}

.status-warning {
  background: #fed7aa;
  color: #92400e;
}

.status-critical {
  background: #fee2e2;
  color: #991b1b;
}

.status-unknown {
  background: #e5e7eb;
  color: #4b5563;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.metric-card {
  background: #f9fafb;
  padding: 1rem;
  border-radius: var(--radius-md);
  border: 1px solid #e5e7eb;
}

.metric-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 500;
  margin-bottom: 0.5rem;
}

.metric-value {
  font-size: 1.875rem;
  font-weight: 700;
  color: var(--primary-color);
  line-height: 1.2;
}

.metric-range {
  margin-top: 0.5rem;
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

.metric-target {
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: #059669;
  font-weight: 500;
}

.warning-high {
  color: #d97706;
}

.warning-critical {
  color: #dc2626;
  font-weight: 600;
}
</style>
