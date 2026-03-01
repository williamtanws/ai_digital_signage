<template>
  <div class="panel">
    <h3 class="panel-title">Research Validation Metrics</h3>
    
    <div v-if="!researchMetrics" class="panel-empty">
      <p>No research metrics available yet</p>
      <small>Validation data will appear after ETL process runs</small>
    </div>
    
    <div v-else class="panel-content">
      <!-- Face Detection Section -->
      <div class="section">
        <h4 class="section-title">Face Detection Accuracy</h4>
        <div class="accuracy-display">
          <div class="accuracy-circle" :class="accuracyClass">
            <span class="accuracy-value">{{ formatPercentage(researchMetrics.faceDetection?.accuracy) }}%</span>
            <span class="accuracy-label">Accuracy</span>
          </div>
          <div class="accuracy-details">
            <div class="detail-item">
              <span class="detail-label">Confidence:</span>
              <span class="detail-value">{{ formatConfidence(researchMetrics.faceDetection?.confidence) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Frames Processed:</span>
              <span class="detail-value">{{ formatNumber(researchMetrics.faceDetection?.framesProcessed) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Faces Detected:</span>
              <span class="detail-value">{{ formatNumber(researchMetrics.faceDetection?.facesDetected) }}</span>
            </div>
            <div class="target-indicator" :class="meetsTargetClass">
              {{ meetsTargetText }}
            </div>
          </div>
        </div>
      </div>
      
      <!-- Gaze Quality Section -->
      <div class="section">
        <h4 class="section-title">Gaze Tracking Quality</h4>
        <div class="quality-grid">
          <div class="quality-bar">
            <div class="quality-label">Valid Keypoints</div>
            <div class="progress-bar">
              <div class="progress-fill" 
                   :style="{ width: `${researchMetrics.gazeQuality?.kptsValidPercent || 0}%` }">
              </div>
            </div>
            <div class="quality-value">{{ formatPercentage(researchMetrics.gazeQuality?.kptsValidPercent) }}%</div>
          </div>
          
          <div class="quality-bar">
            <div class="quality-label">SolvePnP Success</div>
            <div class="progress-bar">
              <div class="progress-fill success" 
                   :style="{ width: `${researchMetrics.gazeQuality?.solvepnpSuccessPercent || 0}%` }">
              </div>
            </div>
            <div class="quality-value">{{ formatPercentage(researchMetrics.gazeQuality?.solvepnpSuccessPercent) }}%</div>
          </div>
          
          <div class="quality-bar">
            <div class="quality-label">Fallback Mode</div>
            <div class="progress-bar">
              <div class="progress-fill fallback" 
                   :style="{ width: `${researchMetrics.gazeQuality?.fallbackPercent || 0}%` }">
              </div>
            </div>
            <div class="quality-value">{{ formatPercentage(researchMetrics.gazeQuality?.fallbackPercent) }}%</div>
          </div>
        </div>
      </div>
      
      <!-- Comparison Section (if available) -->
      <div class="section" v-if="researchMetrics.comparison">
        <h4 class="section-title">Effectiveness Comparison</h4>
        <div class="comparison-grid">
          <div class="comparison-card baseline">
            <div class="comparison-label">Baseline (Static)</div>
            <div class="comparison-value">{{ formatNumber(researchMetrics.comparison.baseline?.avgEngagement) }}s</div>
            <div class="comparison-desc">{{ researchMetrics.comparison.baseline?.condition }}</div>
          </div>
          
          <div class="comparison-card current">
            <div class="comparison-label">Current (Dynamic)</div>
            <div class="comparison-value">{{ formatNumber(researchMetrics.comparison.current?.avgEngagement) }}s</div>
            <div class="comparison-desc">{{ researchMetrics.comparison.current?.condition }}</div>
          </div>
          
          <div class="comparison-card improvement" :class="{ significant: researchMetrics.comparison.improvement?.significant }">
            <div class="comparison-label">Improvement</div>
            <div class="comparison-value">+{{ formatPercentage(researchMetrics.comparison.improvement?.percentage) }}%</div>
            <div class="comparison-desc">
              {{ researchMetrics.comparison.improvement?.significant ? 'Statistically Significant' : 'Not Significant' }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  researchMetrics: {
    type: Object,
    default: null
  }
});

/**
 * Format number with fallback
 */
const formatNumber = (value) => {
  if (value === null || value === undefined) return 'N/A';
  return typeof value === 'number' ? value.toLocaleString() : value;
};

/**
 * Format percentage (no decimals)
 */
const formatPercentage = (value) => {
  if (value === null || value === undefined) return 'N/A';
  return typeof value === 'number' ? Math.round(value) : value;
};

/**
 * Format confidence (0-1 scale to percentage)
 */
const formatConfidence = (value) => {
  if (value === null || value === undefined) return 'N/A';
  
  if (value <= 1) {
    return `${(value * 100).toFixed(1)}%`;
  }
  
  return `${value.toFixed(1)}%`;
};

/**
 * Accuracy status class
 */
const accuracyClass = computed(() => {
  const accuracy = props.researchMetrics?.faceDetection?.accuracy;
  
  if (!accuracy) return '';
  
  if (accuracy >= 94) return 'excellent';
  if (accuracy >= 85) return 'good';
  if (accuracy >= 75) return 'fair';
  return 'poor';
});

/**
 * Meets target indicator
 */
const meetsTargetClass = computed(() => {
  const accuracy = props.researchMetrics?.faceDetection?.accuracy;
  return accuracy && accuracy >= 94 ? 'target-met' : 'target-not-met';
});

const meetsTargetText = computed(() => {
  const accuracy = props.researchMetrics?.faceDetection?.accuracy;
  
  if (!accuracy) return 'Target: >94% accuracy';
  
  return accuracy >= 94 
    ? '✓ Meets research target (>94%)' 
    : `Target: >94% (${Math.round(94 - accuracy)}% below)`;
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
  gap: 2rem;
}

.section {
  border-top: 1px solid #e5e7eb;
  padding-top: 1.5rem;
}

.section:first-child {
  border-top: none;
  padding-top: 0;
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 1rem;
}

/* Face Detection Accuracy */
.accuracy-display {
  display: flex;
  gap: 2rem;
  align-items: center;
}

.accuracy-circle {
  flex-shrink: 0;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 8px solid;
  transition: all 0.3s ease;
}

.accuracy-circle.excellent {
  border-color: #10b981;
  background: #d1fae5;
}

.accuracy-circle.good {
  border-color: #3b82f6;
  background: #dbeafe;
}

.accuracy-circle.fair {
  border-color: #f59e0b;
  background: #fef3c7;
}

.accuracy-circle.poor {
  border-color: #ef4444;
  background: #fee2e2;
}

.accuracy-value {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1;
}

.accuracy-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-top: 0.25rem;
}

.accuracy-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
  border-bottom: 1px solid #f3f4f6;
}

.detail-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.detail-value {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
}

.target-indicator {
  margin-top: 0.5rem;
  padding: 0.5rem 0.75rem;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  font-weight: 600;
  text-align: center;
}

.target-met {
  background: #d1fae5;
  color: #065f46;
}

.target-not-met {
  background: #fed7aa;
  color: #92400e;
}

/* Gaze Quality */
.quality-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.quality-bar {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.quality-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.progress-bar {
  height: 24px;
  background: #f3f4f6;
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #3b82f6;
  transition: width 0.5s ease;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 0.5rem;
}

.progress-fill.success {
  background: #10b981;
}

.progress-fill.fallback {
  background: #f59e0b;
}

.quality-value {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
  text-align: right;
}

/* Comparison */
.comparison-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
}

.comparison-card {
  padding: 1rem;
  border-radius: var(--radius-md);
  text-align: center;
}

.comparison-card.baseline {
  background: #f3f4f6;
  border: 2px solid #d1d5db;
}

.comparison-card.current {
  background: #dbeafe;
  border: 2px solid #3b82f6;
}

.comparison-card.improvement {
  background: #fef3c7;
  border: 2px solid #f59e0b;
}

.comparison-card.improvement.significant {
  background: #d1fae5;
  border: 2px solid #10b981;
}

.comparison-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.5rem;
}

.comparison-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.comparison-desc {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}
</style>
