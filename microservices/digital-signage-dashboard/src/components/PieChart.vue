<template>
  <div class="chart-container">
    <h3 class="chart-title">{{ title }}</h3>
    <div class="chart-wrapper">
      <canvas ref="chartCanvas"></canvas>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { Chart, PieController, ArcElement, Tooltip, Legend } from 'chart.js';

// Register Chart.js components
Chart.register(PieController, ArcElement, Tooltip, Legend);

/**
 * Pie Chart Component
 * 
 * Renders a pie chart using Chart.js.
 * Useful for showing proportional data and distributions.
 * 
 * Props:
 * - title: string - Chart title
 * - labels: array - Slice labels
 * - data: array - Slice values
 * - colors: array (optional) - Custom colors for each slice
 */

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  labels: {
    type: Array,
    required: true
  },
  data: {
    type: Array,
    required: true
  },
  colors: {
    type: Array,
    default: () => [
      '#2563eb', // Blue
      '#10b981', // Green
      '#f59e0b', // Yellow
      '#ef4444', // Red
      '#8b5cf6', // Purple
      '#06b6d4', // Cyan
      '#f97316', // Orange
      '#ec4899'  // Pink
    ]
  }
});

const chartCanvas = ref(null);
let chartInstance = null;

/**
 * Create or update the chart
 */
const createChart = () => {
  if (!chartCanvas.value) return;
  
  // Destroy existing chart if it exists
  if (chartInstance) {
    chartInstance.destroy();
  }
  
  const ctx = chartCanvas.value.getContext('2d');
  
  chartInstance = new Chart(ctx, {
    type: 'pie',
    data: {
      labels: props.labels,
      datasets: [{
        data: props.data,
        backgroundColor: props.colors,
        borderColor: '#ffffff',
        borderWidth: 2
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 15,
            font: {
              size: 12
            },
            usePointStyle: true
          }
        },
        tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          padding: 12,
          titleFont: {
            size: 14
          },
          bodyFont: {
            size: 13
          },
          callbacks: {
            label: function(context) {
              const label = context.label || '';
              const value = context.parsed || 0;
              const total = context.dataset.data.reduce((acc, val) => acc + val, 0);
              const percentage = ((value / total) * 100).toFixed(1);
              return `${label}: ${value} (${percentage}%)`;
            }
          }
        }
      }
    }
  });
};

/**
 * Initialize chart on component mount
 */
onMounted(() => {
  createChart();
});

/**
 * Recreate chart when data changes
 */
watch(() => [props.labels, props.data], () => {
  createChart();
}, { deep: true });
</script>

<style scoped>
/* Component-specific styles are inherited from global .chart-container styles */
</style>
