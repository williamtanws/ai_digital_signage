import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// Raspberry Pi 5 optimized build configuration
// Reduces memory usage and build time for ARM64 architecture

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@assets': path.resolve(__dirname, './src/assets'),
      '@images': path.resolve(__dirname, './src/assets/images'),
      '@banners': path.resolve(__dirname, './src/assets/banners')
    }
  },
  build: {
    // Output directory
    outDir: 'dist',
    
    // Reduce chunk size warnings threshold for embedded systems
    chunkSizeWarningLimit: 1000,
    
    // Optimize for production
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,      // Remove console.logs in production
        drop_debugger: true,
        passes: 1                // Single pass to reduce build time
      }
    },
    
    // Rollup options for multi-page app
    rollupOptions: {
      input: {
        main: path.resolve(__dirname, 'index.html'),
        slideshow: path.resolve(__dirname, 'slideshow.html')
      },
      output: {
        // Manual chunking for better caching
        manualChunks: {
          'vendor': ['vue', 'pinia'],
          'charts': ['echarts', 'vue-echarts']
        },
        // Smaller chunk size for ARM64
        chunkFileNames: 'assets/[name]-[hash].js',
        entryFileNames: 'assets/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]'
      }
    },
    
    // Source maps for debugging (disable in production for smaller size)
    sourcemap: false,
    
    // Asset inlining threshold (bytes) - reduce for Pi
    assetsInlineLimit: 2048,    // 2KB (default is 4KB)
    
    // Enable CSS code splitting
    cssCodeSplit: true,
    
    // Don't emit empty chunks
    emptyOutDir: true
  },
  
  // Production-only settings
  server: {
    port: 5173,
    
    // API proxy for development only
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  
  // Optimization settings
  optimizeDeps: {
    include: ['vue', 'pinia', 'echarts'],
    exclude: []
  },
  
  // Define environment variables
  define: {
    __VUE_PROD_DEVTOOLS__: false,
    __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: false
  }
})
