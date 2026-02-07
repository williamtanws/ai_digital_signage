# AI Digital Signage Installation Guide  
(Raspberry Pi 5 + Hailo-8 + X1200 UPS)

This guide covers operating system dependencies, Python environment setup, AI project installation, Jupyter integration, environmental sensors (BME688), USB microphone support for ambient noise measurement, and UPS auto-shutdown configuration for the AI Digital Signage system.

---

## 1. System Dependencies Installation

Update the system and install required native libraries for camera access, AI inference, audio input, and numerical computation.

```bash
sudo apt update
sudo apt install -y \
  libcamera-dev \
  python3-pyqt5 \
  libatlas-base-dev \
  libcap-dev \
  portaudio19-dev \
  libportaudio2
```

**Note**  
`portaudio19-dev` and `libportaudio2` are required to enable USB microphone access via Python for ambient noise (dB) measurement.

---

## 2. Python Environment Setup

Create and activate the project virtual environment using system site packages to ensure compatibility with Raspberry Pi camera drivers.

```bash
python3 -m venv audience-analysis_env --system-site-packages
source audience-analysis_env/bin/activate
```

Upgrade core Python tooling:

```bash
pip install --upgrade pip setuptools
```

---

## 3. Project Dependency Installation

Install all project-specific Python dependencies:

```bash
pip install -r requirements.txt
```

Install additional camera, sensor, and audio libraries:

```bash
pip install picamera2 bme680 sounddevice
```

---

## 4. Jupyter Notebook Integration

Register the virtual environment as a Jupyter kernel:

```bash
python -m ipykernel install --user \
  --name=audience-analysis_env \
  --display-name "Python (analysis_env)"
```

Launch Jupyter Notebook:

```bash
source audience-analysis_env/bin/activate
jupyter notebook
```

---

## 5. USB Microphone Verification (Ambient Noise Measurement)

Verify that the USB microphone is detected by the operating system:

```bash
arecord -l
```

Expected output example:

```
card 0: Device [USB PnP Sound Device], device 0: USB Audio
```

Verify microphone availability in Python:

```bash
python - <<EOF
import sounddevice as sd
print(sd.query_devices())
EOF
```

**Note**  
The USB microphone is used **only** to measure ambient noise level (dB) as an environmental context indicator.  
No audio recording, speech recognition, or audio content analysis is performed.

---

## 6. EEPROM Configuration for UPS Compatibility

Edit EEPROM settings to ensure power stability and safe shutdown with the X1200 UPS:

```bash
sudo rpi-eeprom-config -e
```

Append or update the following parameters:

```ini
BOOT_UART=1
POWER_OFF_ON_HALT=1
BOOT_ORDER=0xf41
PSU_MAX_CURRENT=5000
```

Apply the changes:

```bash
sudo reboot
```

---

## 7. UPS Auto-Shutdown Setup (X1200 Series)

### Make the Shutdown Script Executable

```bash
chmod +x /home/william/ai_digital_signage/ups/ups_shutdown.py
```

### Create the Systemd Service

```bash
sudo nano /etc/systemd/system/ups-shutdown.service
```

Paste the following configuration:

```ini
[Unit]
Description=Auto Shutdown on Low Battery (X1200 UPS)
After=multi-user.target

[Service]
Type=simple
ExecStart=/usr/bin/python3 /home/william/ai_digital_signage/ups/ups_shutdown.py
Restart=always
User=pi

[Install]
WantedBy=multi-user.target
```

### Enable and Start the Service

```bash
sudo systemctl daemon-reexec
sudo systemctl daemon-reload
sudo systemctl enable ups-shutdown.service
sudo systemctl start ups-shutdown.service
```

---

## 8. Sensor & Hardware Troubleshooting

Detect connected I2C devices (BME688, UPS):

```bash
sudo i2cdetect -y 1
```

---

## 9. Useful Git Commands

```bash
git add .
git commit -m "Initial setup and environment configuration"
git push origin main
```

---

## Dissertation and Privacy Notes

- Ambient noise level is captured using a USB microphone and expressed as approximate sound pressure level (dB).
- Audio signals are processed transiently in memory without storage or content analysis.
- The system adheres to **edge-only processing** and **privacy-by-design** principles suitable for Malaysian SME F&B environments.

---
