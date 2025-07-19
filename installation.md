# AI Digital Signage Installation Guide (Raspberry Pi 5 + X1200 UPS)

This guide covers the setup of Python libraries, project environment, Jupyter, and UPS auto-shutdown service for the AI Digital Signage system.

---

## 1. Python and Picamera2 Libraries Installation

```bash
sudo apt update
sudo apt install -y libcamera-dev python3-pyqt5 libatlas-base-dev libcap-dev
pip install --upgrade pip setuptools
pip install picamera2
```

---

## 2. Project Installation

```bash
python3 -m venv ai_digital_signage_env --system-site-packages
source ai_digital_signage_env/bin/activate
pip install -r requirements.txt
```

---

## 3. Enable Virtual Environment in Jupyter

```bash
python -m ipykernel install --user --name=ai_digital_signage_env --display-name "Python (ai_digital_signage_env)"
pip install bme680
```

---

## 4. Running Jupyter Notebook

```bash
source ai_digital_signage_env/bin/activate
jupyter notebook
```

---

## 5. EEPROM Configuration for UPS Compatibility

Edit the EEPROM settings for power stability and safe shutdown with X1200 UPS:

```bash
sudo rpi-eeprom-config -e
```

Append or update the following in the config file:

```ini
BOOT_UART=1
POWER_OFF_ON_HALT=1
BOOT_ORDER=0xf41
PSU_MAX_CURRENT=5000
```

Then apply the changes:

```bash
sudo reboot
```

---

## 6. UPS Auto Shutdown Setup (X1200 Series)

### Make the Shutdown Script Executable

```bash
chmod +x /home/william/ai_digital_signage/ups/ups_shutdown.py
```

### Create the Systemd Service

```bash
sudo nano /etc/systemd/system/ups-shutdown.service
```

Paste the following content:

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

## 7. Troubleshooting

### Detect Connected I2C Devices

```bash
sudo i2cdetect -y 1
```

---

## 8. Useful Git Commands

```bash
git add .
git commit -m "Init"
git push origin main
```
