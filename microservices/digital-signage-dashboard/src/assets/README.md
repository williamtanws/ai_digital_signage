# Assets Directory

This directory contains static assets for the Digital Signage Dashboard.

## Structure

```
assets/
├── images/         # General images (logos, icons, etc.)
├── banners/        # Banner images for ads and promotions
└── README.md       # This file
```

## Usage

### In Vue Components

```vue
<script setup>
import logo from '@/assets/images/logo.png'
import banner from '@/assets/banners/summer-sale.jpg'
</script>

<template>
  <img :src="logo" alt="Logo" />
  <img :src="banner" alt="Summer Sale Banner" />
</template>
```

### In CSS

```css
.banner {
  background-image: url('@/assets/banners/promotion.jpg');
}
```

### Dynamically

```vue
<script setup>
const getBannerUrl = (name) => {
  return new URL(`../assets/banners/${name}`, import.meta.url).href
}
</script>

<template>
  <img :src="getBannerUrl('sale.jpg')" alt="Sale" />
</template>
```

## Recommended Image Formats

- **Logos/Icons**: PNG (with transparency), SVG
- **Banners**: JPG, WebP (for better compression)
- **Max Size**: Keep images under 500KB for optimal performance

## Image Optimization

Before adding images, consider optimizing them:
- Use [TinyPNG](https://tinypng.com/) for PNG compression
- Use [Squoosh](https://squoosh.app/) for JPG/WebP conversion
- Resize to appropriate dimensions for display
