#!/bin/bash
#
# Production build script for Raspberry Pi 5
# Optimized for ARM64 architecture with reduced memory usage
#

set -e

DASHBOARD_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DASHBOARD_DIR"

echo "========================================================="
echo "  Building Dashboard for Raspberry Pi 5 (Production)"
echo "========================================================="
echo ""

# Check Node.js version
NODE_VERSION=$(node -v)
echo "Node.js version: $NODE_VERSION"

if [[ ! "$NODE_VERSION" =~ ^v(18|20|21|22) ]]; then
  echo "WARNING: Node.js 18+ recommended for best performance"
fi

# Check available memory
AVAILABLE_RAM=$(free -m | awk '/^Mem:/{print $7}')
echo "Available RAM: ${AVAILABLE_RAM}MB"

# Set Node.js memory limit for build (max 2GB on Pi)
export NODE_OPTIONS="--max-old-space-size=2048"

# Clean previous build
echo ""
echo "==> Cleaning previous build..."
rm -rf dist node_modules/.vite
echo "✓ Cleaned"

# Install dependencies (production only)
echo ""
echo "==> Installing dependencies..."
npm ci --production=false --prefer-offline
echo "✓ Dependencies installed"

# Build with Pi-optimized config
echo ""
echo "==> Building application (PI config)..."
npm run build -- --config vite.config.pi.js

if [ $? -eq 0 ]; then
  echo ""
  echo "========================================================="
  echo "  Build Complete!"
  echo "========================================================="
  echo ""
  echo "Output directory: dist/"
  echo "Total size: $(du -sh dist/ | cut -f1)"
  echo ""
  echo "Next steps:"
  echo "  1. Verify build: ls -lh dist/"
  echo "  2. Test locally: npx http-server dist/ -p 8000"
  echo "  3. Deploy with nginx: Copy to /home/pi/ai_digital_signage/microservices/digital-signage-dashboard/dist/"
  echo "  4. Restart nginx: sudo systemctl reload nginx"
  echo ""
else
  echo ""
  echo "❌ Build failed! Check the error messages above."
  exit 1
fi
