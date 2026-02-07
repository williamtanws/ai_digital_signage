<template>
  <div class="card">
    <div class="card-title">{{ title }}</div>
    <div class="card-value">{{ formattedValue }}</div>
    <div class="card-label" v-if="label">{{ label }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

/**
 * KPI Card Component
 * 
 * Displays a key performance indicator with:
 * - Title (descriptor)
 * - Value (main metric)
 * - Optional label (unit or additional context)
 * 
 * Props:
 * - title: string - The KPI title/name
 * - value: number/string - The KPI value
 * - label: string (optional) - Additional label text
 * - format: string (optional) - Format type ('number', 'decimal', 'none')
 */

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [Number, String],
    required: true
  },
  label: {
    type: String,
    default: ''
  },
  format: {
    type: String,
    default: 'number',
    validator: (value) => ['number', 'decimal', 'none'].includes(value)
  }
});

/**
 * Format the value based on the specified format type
 */
const formattedValue = computed(() => {
  if (props.format === 'none') {
    return props.value;
  }
  
  if (props.format === 'decimal') {
    return typeof props.value === 'number' 
      ? props.value.toFixed(1)
      : props.value;
  }
  
  // Default: number format with thousands separator
  if (typeof props.value === 'number') {
    return props.value.toLocaleString();
  }
  
  return props.value;
});
</script>

<style scoped>
/* Component-specific styles are inherited from global .card styles */
</style>
