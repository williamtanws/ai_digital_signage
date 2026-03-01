<template>
  <div class="dashboard-container">
    <!-- Header -->
    <header class="dashboard-header">
      <h1 class="dashboard-title">Audience Analysis Dashboard</h1>
      <p class="dashboard-subtitle">Edge AI Digital Signage Analytics & Reporting</p>
    </header>

    <!-- Loading State -->
    <div v-if="loading" class="loading">
      <p>Loading dashboard data...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error">
      <strong>Error:</strong> {{ error }}
      <br>
      <button @click="loadDashboardData" style="margin-top: 10px;">Retry</button>
    </div>

    <!-- Dashboard Content -->
    <div v-else>
      <!-- KPI Cards -->
      <section class="kpi-grid">
        <KpiCard
          title="Total Audience"
          :value="dashboardData.totalAudience"
          label="Unique Visitors"
          format="number"
        />
        <KpiCard
          title="Total Views"
          :value="dashboardData.totalViews"
          label="Content Impressions"
          format="number"
        />
        <KpiCard
          title="Average View Time"
          :value="dashboardData.avgViewSeconds"
          label="Seconds"
          format="decimal"
        />
        <KpiCard
          title="System FPS"
          :value="dashboardData.systemHealth?.performance?.currentFps || 'N/A'"
          label="Frames Per Second"
          format="decimal"
        />
        <KpiCard
          title="Detection Accuracy"
          :value="detectionAccuracy"
          label="Face Detection"
          format="none"
        />
        <KpiCard
          title="CPU Temperature"
          :value="cpuTemperature"
          label="Raspberry Pi"
          format="none"
        />
      </section>
      
      <!-- Research Validation Panels -->
      <section class="research-panels">
        <SystemHealthPanel :systemHealth="dashboardData.systemHealth" />
        <ResearchMetricsPanel :researchMetrics="dashboardData.researchMetrics" />
      </section>
      
      <!-- Environment Context -->
      <section class="environment-section">
        <EnvironmentContext :environment="dashboardData.systemHealth?.environment" />
      </section>

      <!-- Charts Section -->
      <section class="charts-grid">
        <!-- Age Distribution -->
        <BarChart
          title="Age Distribution"
          :labels="ageLabels"
          :data="ageData"
          label="People"
          color="#2563eb"
        />

        <!-- Ads Performance -->
        <BarChart
          title="Advertisement Performance"
          :labels="adsPerformanceLabels"
          :data="adsPerformanceData"
          label="Total Viewers"
          :horizontal="true"
          color="#10b981"
        />

        <!-- Gender Distribution -->
        <PieChart
          title="Gender Distribution"
          :labels="genderLabels"
          :data="genderData"
          :colors="['#2563eb', '#ec4899']"
        />

        <!-- Emotion Distribution -->
        <BarChart
          title="Emotion Distribution"
          :labels="emotionLabels"
          :data="emotionData"
          label="People"
          color="#f59e0b"
        />

        <!-- Ads Attention -->
        <BarChart
          title="Advertisement Attention (Look vs No Look)"
          :labels="adsAttentionLabels"
          :data="adsAttentionLookYes"
          label="Looked"
          :horizontal="true"
          color="#06b6d4"
        />

        <!-- View vs Look Ratio (calculated from attention data) -->
        <PieChart
          title="Overall Attention Rate"
          :labels="['Looked', 'Did Not Look']"
          :data="[totalLookYes, totalLookNo]"
          :colors="['#10b981', '#ef4444']"
        />
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import KpiCard from './components/KpiCard.vue';
import BarChart from './components/BarChart.vue';
import PieChart from './components/PieChart.vue';
import SystemHealthPanel from './components/SystemHealthPanel.vue';
import EnvironmentContext from './components/EnvironmentContext.vue';
import ResearchMetricsPanel from './components/ResearchMetricsPanel.vue';
import { dashboardApi } from './services/api.js';

/**
 * Audience Analysis Dashboard - Main Application
 * 
 * This dashboard fetches data from the backend API and displays:
 * - 4 KPI cards (audience, views, ads, average time)
 * - 6 charts (age, ads performance, gender, emotion, ads attention, overall attention)
 * 
 * Data is fetched once on mount from a single API endpoint.
 * The dashboard uses Vue 3 Composition API for reactive state management.
 */

// ===================================
// Reactive State
// ===================================

const loading = ref(true);
const error = ref(null);
const dashboardData = ref({
  totalAudience: 0,
  totalViews: 0,
  totalAds: 0,
  avgViewSeconds: 0,
  ageDistribution: {},
  genderDistribution: {},
  emotionDistribution: {},
  adsPerformance: [],
  adsAttention: [],
  systemHealth: null,
  researchMetrics: null
});

// ===================================
// Computed Properties for Charts
// ===================================

/**
 * Age Distribution Chart Data
 */
const ageLabels = computed(() => [
  'Children',
  'Teenagers',
  'Young Adults',
  'Mid-Aged',
  'Seniors'
]);

const ageData = computed(() => {
  const dist = dashboardData.value.ageDistribution;
  return [
    dist.children || 0,
    dist.teenagers || 0,
    dist.youngAdults || 0,
    dist.midAged || 0,
    dist.seniors || 0
  ];
});

/**
 * Gender Distribution Chart Data
 */
const genderLabels = computed(() => ['Male', 'Female']);

const genderData = computed(() => {
  const dist = dashboardData.value.genderDistribution;
  return [
    dist.male || 0,
    dist.female || 0
  ];
});

/**
 * Emotion Distribution Chart Data (FER2013 - 8 emotions)
 */
const emotionLabels = computed(() => [
  'Anger',
  'Contempt',
  'Disgust',
  'Fear',
  'Happiness',
  'Neutral',
  'Sadness',
  'Surprise'
]);

const emotionData = computed(() => {
  const dist = dashboardData.value.emotionDistribution;
  return [
    dist.anger || 0,
    dist.contempt || 0,
    dist.disgust || 0,
    dist.fear || 0,
    dist.happiness || 0,
    dist.neutral || 0,
    dist.sadness || 0,
    dist.surprise || 0
  ];
});

/**
 * Ads Performance Chart Data
 */
const adsPerformanceLabels = computed(() => {
  return dashboardData.value.adsPerformance.map(ad => ad.adName);
});

const adsPerformanceData = computed(() => {
  return dashboardData.value.adsPerformance.map(ad => ad.totalViewers);
});

/**
 * Ads Attention Chart Data
 */
const adsAttentionLabels = computed(() => {
  return dashboardData.value.adsAttention.map(ad => ad.adName);
});

const adsAttentionLookYes = computed(() => {
  return dashboardData.value.adsAttention.map(ad => ad.lookYes);
});

const adsAttentionLookNo = computed(() => {
  return dashboardData.value.adsAttention.map(ad => ad.lookNo);
});

/**
 * Overall Attention Rate (Pie Chart)
 * Calculates total looks vs total no-looks across all ads
 */
const totalLookYes = computed(() => {
  return dashboardData.value.adsAttention.reduce((sum, ad) => sum + ad.lookYes, 0);
});

const totalLookNo = computed(() => {
  return dashboardData.value.adsAttention.reduce((sum, ad) => sum + ad.lookNo, 0);
});

/**
 * Detection Accuracy formatted for KPI card
 */
const detectionAccuracy = computed(() => {
  const accuracy = dashboardData.value.researchMetrics?.faceDetection?.accuracy;
  return accuracy ? `${Math.round(accuracy)}%` : 'N/A';
});

/**
 * CPU Temperature formatted for KPI card
 */
const cpuTemperature = computed(() => {
  const temp = dashboardData.value.systemHealth?.performance?.currentCpuTemp;
  return temp ? `${temp.toFixed(1)}°C` : 'N/A';
});

// ===================================
// Data Fetching
// ===================================

/**
 * Load dashboard data from the backend API
 */
const loadDashboardData = async () => {
  loading.value = true;
  error.value = null;

  try {
    const data = await dashboardApi.getDashboardOverview();
    dashboardData.value = data;
  } catch (err) {
    error.value = err.message;
    console.error('Failed to load dashboard data:', err);
  } finally {
    loading.value = false;
  }
};

/**
 * Load data on component mount
 */
onMounted(() => {
  loadDashboardData();
});
</script>

<style scoped>
/* Component-specific styles are inherited from global styles */

button {
  background-color: var(--primary-color);
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: var(--font-size-sm);
}

button:hover {
  background-color: var(--primary-dark);
}

/* Research Panels Section */
.research-panels {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

/* Environment Section */
.environment-section {
  margin-bottom: 2rem;
}

@media (max-width: 1200px) {
  .research-panels {
    grid-template-columns: 1fr;
  }
}
</style>
