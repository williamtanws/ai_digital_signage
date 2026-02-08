{
 "cells": [
  {
   "cell_type": "code",
   "execution_count": null,
   "id": "8027e9d9-85e8-466f-b45d-b40e75c0b689",
   "metadata": {},
   "outputs": [],
   "source": [
    "#!/usr/bin/env python3\n",
    "\"\"\"Test picamera2 basic functionality\"\"\"\n",
    "\n",
    "print(\"Testing picamera2 import and initialization...\")\n",
    "\n",
    "try:\n",
    "    from picamera2 import Picamera2\n",
    "    print(\"✓ picamera2 imported successfully\")\n",
    "    \n",
    "    # Initialize camera\n",
    "    picam2 = Picamera2()\n",
    "    print(\"✓ Camera object created\")\n",
    "    \n",
    "    # Get camera configuration\n",
    "    camera_config = picam2.create_preview_configuration()\n",
    "    print(f\"✓ Camera configuration created: {camera_config}\")\n",
    "    \n",
    "    # Configure camera\n",
    "    picam2.configure(camera_config)\n",
    "    print(\"✓ Camera configured\")\n",
    "    \n",
    "    # Start camera\n",
    "    picam2.start()\n",
    "    print(\"✓ Camera started\")\n",
    "    \n",
    "    # Capture a test frame\n",
    "    import numpy as np\n",
    "    frame = picam2.capture_array()\n",
    "    print(f\"✓ Frame captured: shape={frame.shape}, dtype={frame.dtype}\")\n",
    "    \n",
    "    # Stop camera\n",
    "    picam2.stop()\n",
    "    print(\"✓ Camera stopped\")\n",
    "    \n",
    "    print(\"\\n✅ All tests passed! picamera2 is working correctly.\")\n",
    "    \n",
    "except ImportError as e:\n",
    "    print(f\"❌ Import error: {e}\")\n",
    "except Exception as e:\n",
    "    print(f\"❌ Runtime error: {e}\")\n",
    "    import traceback\n",
    "    traceback.print_exc()"
   ]
  }
 ],
 "metadata": {
  "kernelspec": {
   "display_name": "Python 3 (ipykernel)",
   "language": "python",
   "name": "python3"
  },
  "language_info": {
   "codemirror_mode": {
    "name": "ipython",
    "version": 3
   },
   "file_extension": ".py",
   "mimetype": "text/x-python",
   "name": "python",
   "nbconvert_exporter": "python",
   "pygments_lexer": "ipython3",
   "version": "3.11.2"
  }
 },
 "nbformat": 4,
 "nbformat_minor": 5
}
