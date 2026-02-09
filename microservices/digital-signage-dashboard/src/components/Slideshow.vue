<template>
  <div class="slideshow-container">
    <transition name="fade" mode="out-in">
      <div 
        :key="currentIndex" 
        class="slide"
        :style="{ backgroundImage: `url(${currentImage})` }"
      >
        <div class="slide-overlay">
          <h1 class="slide-title">{{ currentImageName }}</h1>
          <div class="slide-indicator">
            {{ currentIndex + 1 }} / {{ images.length }}
          </div>
        </div>
      </div>
    </transition>
    
    <!-- Navigation Controls (optional, hidden by default) -->
    <div class="controls" v-if="showControls">
      <button @click="previousSlide" class="control-btn">❮</button>
      <button @click="toggleAutoPlay" class="control-btn">
        {{ isPlaying ? '⏸' : '▶' }}
      </button>
      <button @click="nextSlide" class="control-btn">❯</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'

// Import all signage images
import breakfast from '@/assets/images/signage/breakfast.png'
import burger from '@/assets/images/signage/burger.png'
import coffee from '@/assets/images/signage/coffee.png'
import pizza from '@/assets/images/signage/pizza.png'
import salad from '@/assets/images/signage/salad.png'

const images = [
  { src: breakfast, name: 'Fresh Breakfast' },
  { src: burger, name: 'Delicious Burger' },
  { src: coffee, name: 'Fresh Coffee' },
  { src: pizza, name: 'Hot Pizza' },
  { src: salad, name: 'Healthy Salad' }
]

const currentIndex = ref(0)
const isPlaying = ref(true)
const showControls = ref(false)
let intervalId = null

const currentImage = computed(() => images[currentIndex.value]?.src || '')
const currentImageName = computed(() => images[currentIndex.value]?.name || '')

const nextSlide = () => {
  currentIndex.value = (currentIndex.value + 1) % images.length
}

const previousSlide = () => {
  currentIndex.value = currentIndex.value === 0 ? images.length - 1 : currentIndex.value - 1
}

const startAutoPlay = () => {
  if (intervalId) clearInterval(intervalId)
  intervalId = setInterval(() => {
    nextSlide()
  }, 5000) // 5 seconds
  isPlaying.value = true
}

const stopAutoPlay = () => {
  if (intervalId) {
    clearInterval(intervalId)
    intervalId = null
  }
  isPlaying.value = false
}

const toggleAutoPlay = () => {
  if (isPlaying.value) {
    stopAutoPlay()
  } else {
    startAutoPlay()
  }
}

// Show controls on mouse move
let hideControlsTimeout = null
const handleMouseMove = () => {
  showControls.value = true
  clearTimeout(hideControlsTimeout)
  hideControlsTimeout = setTimeout(() => {
    showControls.value = false
  }, 3000)
}

// Keyboard controls
const handleKeydown = (event) => {
  switch (event.key) {
    case 'ArrowLeft':
      previousSlide()
      break
    case 'ArrowRight':
      nextSlide()
      break
    case ' ':
    case 'Spacebar':
      event.preventDefault()
      toggleAutoPlay()
      break
    case 'Escape':
      // Exit fullscreen if supported
      if (document.fullscreenElement) {
        document.exitFullscreen()
      }
      break
  }
}

onMounted(() => {
  startAutoPlay()
  window.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  stopAutoPlay()
  window.removeEventListener('mousemove', handleMouseMove)
  window.removeEventListener('keydown', handleKeydown)
  clearTimeout(hideControlsTimeout)
})
</script>

<style scoped>
.slideshow-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: #000;
  overflow: hidden;
}

.slide {
  width: 100%;
  height: 100%;
  background-size: contain;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
}

.slide-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  padding: 40px;
  color: white;
}

.slide-title {
  font-size: 3rem;
  font-weight: bold;
  margin: 0 0 10px 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8);
}

.slide-indicator {
  font-size: 1.5rem;
  opacity: 0.8;
}

/* Fade transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.8s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Controls */
.controls {
  position: absolute;
  bottom: 50%;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  padding: 0 20px;
  transform: translateY(50%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.slideshow-container:hover .controls {
  opacity: 1;
}

.control-btn {
  background: rgba(255, 255, 255, 0.3);
  border: none;
  color: white;
  font-size: 2rem;
  padding: 15px 25px;
  cursor: pointer;
  border-radius: 5px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.control-btn:hover {
  background: rgba(255, 255, 255, 0.5);
  transform: scale(1.1);
}

.control-btn:active {
  transform: scale(0.95);
}

/* Responsive */
@media (max-width: 768px) {
  .slide-title {
    font-size: 2rem;
  }
  
  .slide-indicator {
    font-size: 1rem;
  }
  
  .control-btn {
    font-size: 1.5rem;
    padding: 10px 20px;
  }
}
</style>
