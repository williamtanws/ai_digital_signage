#!/usr/bin/env python3
"""
Hailo-8 Temperature Monitor
Continuous live monitoring with color-coded terminal output.
Compatible with Raspberry Pi AI Kit (Hailo-8L) - temperature only.
"""

import time
import sys
import signal

REFRESH_INTERVAL = 2.0  # seconds between reads

# ANSI color codes
RESET  = "\033[0m"
BOLD   = "\033[1m"
GREEN  = "\033[92m"
YELLOW = "\033[93m"
RED    = "\033[91m"
CYAN   = "\033[96m"
DIM    = "\033[2m"

# Temperature zone thresholds (°C)
ZONE_NORMAL    = 70.0
ZONE_OVERCLOCK = 80.0
ZONE_HIGH      = 90.0


def get_color(temp: float) -> str:
    if temp < ZONE_NORMAL:
        return GREEN
    elif temp < ZONE_OVERCLOCK:
        return YELLOW
    elif temp < ZONE_HIGH:
        return RED
    else:
        return RED + BOLD


def get_zone_label(temp: float) -> str:
    if temp < ZONE_NORMAL:
        return "NORMAL"
    elif temp < ZONE_OVERCLOCK:
        return "THROTTLE"
    elif temp < ZONE_HIGH:
        return "HIGH"
    else:
        return "CRITICAL"


def format_bar(temp: float, width: int = 20) -> str:
    pct = min(temp / 100.0, 1.0)
    filled = int(pct * width)
    bar = "█" * filled + "░" * (width - filled)
    return f"[{get_color(temp)}{bar}{RESET}]"


def clear_line():
    sys.stdout.write("\r\033[K")
    sys.stdout.flush()


def print_header():
    print(f"\n{BOLD}{CYAN}  Hailo-8 Temperature Monitor{RESET}")
    print(f"{DIM}  Press Ctrl+C to exit\n{RESET}")
    print(f"  {'TEMP':>8}  {'ZONE':>10}  {'BAR':<24}  ELAPSED")
    print(f"  {'─'*8}  {'─'*10}  {'─'*24}  {'─'*8}")


def run_monitor():
    try:
        from hailo_platform import Device
    except ImportError:
        print(f"\n{RED}[ERROR]{RESET} hailo_platform not found.")
        print("  Activate your Hailo virtualenv first:")
        print("  $ source ~/hailo-venv/bin/activate")
        sys.exit(1)

    try:
        device = Device()
    except Exception as e:
        print(f"\n{RED}[ERROR]{RESET} Could not open Hailo device: {e}")
        print("  Is the Hailo-8 connected and driver loaded?")
        sys.exit(1)

    print_header()

    start_time = time.time()
    running = True

    def handle_sigint(sig, frame):
        nonlocal running
        running = False

    signal.signal(signal.SIGINT, handle_sigint)

    while running:
        try:
            temp = device.control.get_chip_temperature().ts0_temperature
            elapsed = int(time.time() - start_time)
            elapsed_str = f"{elapsed // 60:02d}:{elapsed % 60:02d}"

            color = get_color(temp)
            zone  = get_zone_label(temp)
            bar   = format_bar(temp)

            clear_line()
            sys.stdout.write(
                f"  {color}{temp:>7.2f}°C{RESET}  "
                f"{color}{zone:>10}{RESET}  "
                f"{bar}  "
                f"{DIM}{elapsed_str}{RESET}"
            )
            sys.stdout.flush()

            time.sleep(REFRESH_INTERVAL)

        except Exception as e:
            clear_line()
            print(f"\n{RED}[ERROR]{RESET} Read failed: {e}")
            time.sleep(REFRESH_INTERVAL)

    print(f"\n\n{DIM}  Monitor stopped.{RESET}\n")


if __name__ == "__main__":
    run_monitor()
