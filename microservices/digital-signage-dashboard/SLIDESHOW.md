# Digital Signage Slideshow - Quick Start

## 🚀 Quick Access

**Development:** http://localhost:5173/slideshow.html  
**Production:** https://your-domain.com/slideshow.html

## ⌨️ Keyboard Controls

| Key | Action |
|-----|--------|
| **Space** | Pause/Play |
| **←** | Previous slide |
| **→** | Next slide |
| **Esc** | Exit fullscreen |

## 🖱️ Mouse Controls

- **Hover** - Show navigation buttons
- **Click Previous/Next** - Navigate slides
- **Click Play/Pause** - Control playback

## 🎨 Current Images

Located in: `src/assets/images/signage/`

1. burger.png - Delicious Burger
2. coffee.png - Fresh Coffee
3. pizza.png - Hot Pizza
4. salad.png - Healthy Salad

## ➕ Adding New Images

1. **Add image file** to `src/assets/images/signage/`
2. **Edit** `src/components/Slideshow.vue`:

```javascript
// Import your image
import myImage from '@/assets/images/signage/my-image.jpg'

// Add to images array
const images = [
  { src: burger, name: 'Delicious Burger' },
  { src: coffee, name: 'Fresh Coffee' },
  { src: pizza, name: 'Hot Pizza' },
  { src: salad, name: 'Healthy Salad' },
  { src: myImage, name: 'My Image Title' } // Add this
]
```

3. **Restart dev server** to see changes

## ⚙️ Customization

### Change Rotation Speed

**File:** `src/components/Slideshow.vue`  
**Line:** ~52

```javascript
intervalId = setInterval(() => {
  nextSlide()
}, 5000) // Change to desired milliseconds (e.g., 3000 = 3 seconds)
```

### Change Image Display Mode

**File:** `src/components/Slideshow.vue`  
**Styles section**

```css
.slide {
  background-size: contain;  /* Options: contain | cover | 100% */
  background-position: center;
}
```

- `contain` - Fit entire image (may have letterboxing)
- `cover` - Fill screen (may crop image)
- `100%` - Stretch to fill (may distort)

### Change Transition Effect

**File:** `src/components/Slideshow.vue`  
**Styles section**

```css
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.8s ease; /* Adjust speed and easing */
}
```

## 🖥️ Deployment for Signage Screens

### Chrome Kiosk Mode (Recommended)

```bash
chrome --kiosk --app=http://localhost:5173/slideshow.html
```

### Full-Screen Tips

1. Press **F11** for browser fullscreen
2. Disable screen sleep in OS settings
3. Disable screensaver
4. Set browser to launch on startup
5. Use browser's kiosk mode for security

### Auto-Start on Windows

Create `slideshow.bat`:
```batch
@echo off
start chrome --kiosk --app=http://localhost:5173/slideshow.html
```

Add to Windows Startup folder:
```
%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup
```

### Auto-Start on Linux

Add to `~/.config/autostart/slideshow.desktop`:
```ini
[Desktop Entry]
Type=Application
Name=Digital Signage
Exec=chromium-browser --kiosk --app=http://localhost:5173/slideshow.html
```

## 🛠️ Troubleshooting

### Slideshow page not loading?

1. Ensure dev server is running: `npm run dev`
2. Check console for errors
3. Verify images exist in `src/assets/images/signage/`
4. Restart dev server after config changes

### Images not displaying?

1. Check image paths in `Slideshow.vue`
2. Verify image import statements
3. Ensure images are in correct directory
4. Check browser console for 404 errors

### Controls not working?

1. Check browser console for JavaScript errors
2. Ensure keyboard is focused on page (click page first)
3. Try different browser

## 📊 Features Overview

- ✅ **Auto-rotation** (5 second intervals)
- ✅ **Smooth transitions** (fade effect)
- ✅ **Full-screen optimized**
- ✅ **Keyboard navigation**
- ✅ **Mouse controls on hover**
- ✅ **Progress indicator**
- ✅ **Pause/Resume**
- ✅ **Responsive design**

## 🔗 Related Files

- `slideshow.html` - Entry HTML page
- `src/slideshow.js` - Entry point script
- `src/components/Slideshow.vue` - Main component
- `vite.config.js` - Multi-page configuration
- `src/assets/images/signage/` - Image directory

## 📱 Need Help?

See full documentation in `README.md` - Search for "Digital Signage Slideshow" section.
