#!/usr/bin/env python3
"""Test picamera2 basic functionality"""

print("Testing picamera2 import and initialization...")

try:
    from picamera2 import Picamera2
    print("✓ picamera2 imported successfully")
    
    # Initialize camera
    picam2 = Picamera2()
    print("✓ Camera object created")
    
    # Get camera configuration
    camera_config = picam2.create_preview_configuration()
    print(f"✓ Camera configuration created: {camera_config}")
    
    # Configure camera
    picam2.configure(camera_config)
    print("✓ Camera configured")
    
    # Start camera
    picam2.start()
    print("✓ Camera started")
    
    # Capture a test frame
    import numpy as np
    frame = picam2.capture_array()
    print(f"✓ Frame captured: shape={frame.shape}, dtype={frame.dtype}")
    
    # Stop camera
    picam2.stop()
    print("✓ Camera stopped")
    
    print("\n✅ All tests passed! picamera2 is working correctly.")
    
except ImportError as e:
    print(f"❌ Import error: {e}")
except Exception as e:
    print(f"❌ Runtime error: {e}")
    import traceback
    traceback.print_exc()