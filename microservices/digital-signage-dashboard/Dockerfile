# ==============================================
# Digital Signage Dashboard - Raspberry Pi Optimized
# ==============================================
# Vite dev server for development/testing
# ARM64 compatible for Raspberry Pi 4/5

FROM node:20-alpine

WORKDIR /app

# Copy package files for dependency caching
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source code
COPY . .

EXPOSE 5173

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:5173/ || exit 1

# Run Vite dev server with host 0.0.0.0 to accept external connections
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0", "--port", "5173"]
