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
import { Chart, BarController, BarElement, CategoryScale, LinearScale, Title, Tooltip, Legend } from 'chart.js';

// Register Chart.js components
Chart.register(BarController, BarElement, CategoryScale, LinearScale, Title, Tooltip, Legend);

/**
 * Bar Chart Component
 * 
 * Renders a bar chart using Chart.js.
 * Supports both vertical and horizontal orientations.
 * 
 * Props:
 * - title: string - Chart title
 * - labels: array - X-axis labels
 * - data: array - Y-axis data values
 * - label: string - Dataset label
 * - horizontal: boolean - Whether to display horizontal bars
 * - color: string (optional) - Bar color (hex or rgb)
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
  label: {
    type: String,
    default: 'Value'
  },
  horizontal: {
    type: Boolean,
    default: false
  },
  color: {
    type: String,
    default: '#2563eb'
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
    type: 'bar',
    data: {
      labels: props.labels,
      datasets: [{
        label: props.label,
        data: props.data,
        backgroundColor: props.color,
        borderColor: props.color,
        borderWidth: 1,
        borderRadius: 4
      }]
    },
    options: {
      indexAxis: props.horizontal ? 'y' : 'x',
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
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
              return `${props.label}: ${context.parsed.y || context.parsed.x}`;
            }
          }
        }
      },
      scales: {
        x: {
          grid: {
            display: !props.horizontal
          },
          ticks: {
            font: {
              size: 11
            }
          }
        },
        y: {
          grid: {
            display: props.horizontal
          },
          ticks: {
            font: {
              size: 11
            }
          },
          beginAtZero: true
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
