# -*- coding: utf-8 -*-
#!/usr/bin/env python3

import os
import time
import smbus2
import logging
from logging.handlers import TimedRotatingFileHandler
from datetime import datetime
from pathlib import Path

# I2C configuration
I2C_BUS = 1
MAX17048_ADDR = 0x36
bus = smbus2.SMBus(I2C_BUS)

# --- Setup Logging ---
SCRIPT_DIR = Path(__file__).resolve().parent
LOG_DIR = SCRIPT_DIR.parent / "logs"
LOG_DIR.mkdir(parents=True, exist_ok=True)
LOG_FILE = LOG_DIR / "ups-shutdown.log"

log_handler = TimedRotatingFileHandler(
    filename=LOG_FILE,
    when="H",                # Rotate every hour
    interval=1,
    backupCount=4,           # Keep last 4 logs (4 hours)
    utc=True
)
formatter = logging.Formatter("[%(asctime)s] %(message)s")
log_handler.setFormatter(formatter)

logger = logging.getLogger("UPSShutdown")
logger.setLevel(logging.INFO)
logger.addHandler(log_handler)
logger.propagate = False

# --- Read battery info ---
def read_voltage():
    raw = bus.read_word_data(MAX17048_ADDR, 0x02)
    raw = ((raw & 0xFF) << 8) | (raw >> 8)
    return raw * 78.125 / 1000000  # convert microvolts to volts

def read_soc():
    raw = bus.read_word_data(MAX17048_ADDR, 0x04)
    raw = ((raw & 0xFF) << 8) | (raw >> 8)
    return raw / 256  # state of charge in %

# --- Main loop ---
def main():
    while True:
        try:
            voltage = read_voltage()
            soc = read_soc()
            logger.info(f"Battery: {soc:.2f}% | Voltage: {voltage:.2f}V")

            if soc < 20.0 and voltage < 3.7:
                logger.warning("Low battery AND AC disconnected. Initiating shutdown...")
                os.system("sudo shutdown -h now")
                break

        except Exception as e:
            logger.error(f"ERROR: {e}")

        time.sleep(60)

if __name__ == "__main__":
    main()
