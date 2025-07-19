# -*- coding: utf-8 -*-
import os
from hailo_platform import Device

# Get Raspberry Pi CPU temperature
def get_rpi_cpu_temp():
    try:
        with open("/sys/class/thermal/thermal_zone0/temp", "r") as f:
            temp_milli_c = int(f.read().strip())
        return temp_milli_c / 1000.0  # Convert to Celsius
    except FileNotFoundError:
        return None

# Get Hailo-8 temperature(s)
def get_hailo_temp():
    temps = []
    devices = [Device(di) for di in Device.scan()]
    for dev in devices:
        temp_info = dev.control.get_chip_temperature()
        # Safely get only known attribute(s)
        temps.append({
            "device_id": dev.device_id,
            "ts0_temp": temp_info.ts0_temperature
        })
    return temps

# Main
if __name__ == "__main__":
    rpi_temp = get_rpi_cpu_temp()
    print(f"Raspberry Pi CPU Temp: {rpi_temp:.2f} Celcius")

    hailo_temps = get_hailo_temp()
    for t in hailo_temps:
        print(f"Hailo Device {t['device_id']}: ts0={t['ts0_temp']:.2f} Celcius")
