#!/bin/bash
# ============================================================
#  Raspberry Pi 5 — System Monitor
#  Uses `watch` to refresh — zero flicker, zero overlay.
# ============================================================

SCRIPT="/tmp/pi5_sysmon_once.py"

cat > "$SCRIPT" << 'PYEOF'
#!/usr/bin/env python3
"""
Single-shot system snapshot for Raspberry Pi 5.
Run via: watch -n 5 python3 /tmp/pi5_sysmon_once.py
watch handles all screen redraw — no flicker, no overlay.
"""

import subprocess, os, re, time

# ANSI
RESET   = "\033[0m";  BOLD    = "\033[1m";  DIM     = "\033[2m"
GREEN   = "\033[92m"; YELLOW  = "\033[93m"; RED     = "\033[91m"
CYAN    = "\033[96m"; BLUE    = "\033[94m"; MAGENTA = "\033[95m"
WHITE   = "\033[97m"

TEMP_WARN = 70.0;  TEMP_HIGH = 80.0
USE_WARN  = 65;    USE_HIGH  = 85

# Hailo thresholds — same as hailo_temp.sh
ZONE_NORMAL    = 70.0
ZONE_OVERCLOCK = 80.0
ZONE_HIGH      = 90.0

_ANSI = re.compile(r"\033\[[0-9;]*m")
def vlen(s):    return len(_ANSI.sub("", s))
def rpad(s, n): return s + " " * max(n - vlen(s), 0)

def run(cmd):
    try:
        return subprocess.check_output(
            cmd, shell=True, stderr=subprocess.DEVNULL, text=True).strip()
    except Exception:
        return ""

def term_width():
    try:    return os.get_terminal_size().columns
    except: return 100

def hr(char="─", color=DIM):
    print(f"{color}{char * term_width()}{RESET}")

def zone_header(label, color=CYAN):
    print()
    print(f"{color}{BOLD}  ▌ {label}{RESET}")
    print()

def temp_color(t):
    try:    t = float(t)
    except: return DIM
    if t >= TEMP_HIGH: return RED + BOLD
    if t >= TEMP_WARN: return YELLOW
    return GREEN

def use_color(pct):
    if pct >= USE_HIGH: return RED
    if pct >= USE_WARN: return YELLOW
    return GREEN

def temp_zone(t):
    try:    t = float(t)
    except: return "UNKNOWN"
    if t >= TEMP_HIGH: return "HIGH"
    if t >= TEMP_WARN: return "WARM"
    return "NORMAL"

def bar(value, width=24, unit=""):
    try:    pct = min(max(int(float(value)), 0), 100)
    except: pct = 0
    filled = int(pct * width / 100)
    color  = use_color(pct)
    b = f"{color}{'█' * filled}{DIM}{'░' * (width - filled)}{RESET}"
    return f"[{b}] {color}{pct:3d}%{RESET}{unit}"

def dot(state):
    return {"ok":   f"{GREEN}●{RESET}",
            "warn": f"{YELLOW}●{RESET}",
            "err":  f"{RED}●{RESET}"}.get(state, f"{DIM}○{RESET}")

# ── Fixed-column row printer ──────────────────────────────────────────────────
C_LBL = 20; C_VAL = 16; C_ZNE = 10

def row(label, val_str, zone_str="", bar_str=""):
    l = f"  {DIM}{label:<{C_LBL}}{RESET}"
    v = rpad(val_str, C_VAL)
    z = rpad(zone_str, C_ZNE)
    print(f"{l}  {v}  {z}  {bar_str}")

def row2(label, val_str):
    print(f"  {DIM}{label:<{C_LBL}}{RESET}  {val_str}")

# ── Hailo — exact same pattern as hailo_temp.sh ───────────────────────────────

# Module-level device singleton — opened once, reused every watch cycle
_hailo_device = None
_hailo_error  = None

def _init_hailo():
    global _hailo_device, _hailo_error
    if _hailo_device is not None:
        return
    try:
        from hailo_platform import Device
        _hailo_device = Device()
    except Exception as e:
        _hailo_error = str(e)

def get_hailo_color(temp):
    if temp < ZONE_NORMAL:    return GREEN
    if temp < ZONE_OVERCLOCK: return YELLOW
    if temp < ZONE_HIGH:      return RED
    return RED + BOLD

def get_hailo_zone(temp):
    if temp < ZONE_NORMAL:    return "NORMAL"
    if temp < ZONE_OVERCLOCK: return "THROTTLE"
    if temp < ZONE_HIGH:      return "HIGH"
    return "CRITICAL"

def format_hailo_bar(temp, width=24):
    pct    = min(max(int(temp), 0), 100)
    filled = int(pct * width / 100)
    color  = get_hailo_color(temp)
    b = f"{color}{'█'*filled}{DIM}{'░'*(width-filled)}{RESET}"
    return f"[{b}] {color}{pct:3d}%{RESET} (°C/100)"

def hailo_temp():
    _init_hailo()
    if _hailo_device is None:
        return None, _hailo_error or "Device not available"
    try:
        t = _hailo_device.control.get_chip_temperature().ts0_temperature
        return float(t), None
    except Exception as e:
        return None, str(e)

# ── Other collectors ──────────────────────────────────────────────────────────

def cpu_temp():
    m = re.search(r"[\d.]+", run("vcgencmd measure_temp"))
    return float(m.group()) if m else 0.0

def cpu_freq_mhz():
    m = re.search(r"\d+", run("vcgencmd measure_clock arm"))
    return int(m.group()) // 1_000_000 if m else 0

def cpu_usage():
    try:
        with open("/proc/stat") as f: l1 = f.readline().split()
        time.sleep(0.5)
        with open("/proc/stat") as f: l2 = f.readline().split()
        t1 = sum(int(x) for x in l1[1:]); i1 = int(l1[4])
        t2 = sum(int(x) for x in l2[1:]); i2 = int(l2[4])
        dt = t2 - t1
        return int(100 * (dt - (i2 - i1)) / dt) if dt > 0 else 0
    except: return 0

def core_temps():
    out = []
    for i in range(4):
        p = f"/sys/class/thermal/thermal_zone{i}/temp"
        if os.path.exists(p):
            try: out.append((i, int(open(p).read().strip()) / 1000))
            except: pass
    return out

def gpu_freq_mhz():
    m = re.search(r"\d+", run("vcgencmd measure_clock core"))
    return int(m.group()) // 1_000_000 if m else 0

def gpu_temp():
    try:
        return round(int(open("/sys/class/thermal/thermal_zone0/temp").read().strip()) / 1000, 1)
    except: return 0.0

def hailo_service(): return run("systemctl is-active hailort.service") or "inactive"
def hailo_driver():  return run("modinfo hailo_pci 2>/dev/null | awk '/^version/{print $2}'") or "---"
def hailo_hmon():
    try:    return len(os.listdir("/tmp/hmon_files"))
    except: return 0

def memory_mb():
    try:
        info = {}
        for l in open("/proc/meminfo"):
            k, v = l.split(":")[0], l.split(":")[1].strip().split()[0]
            info[k] = int(v)
        tot = info.get("MemTotal",0); free = info.get("MemAvailable",0)
        st  = info.get("SwapTotal",0); sf   = info.get("SwapFree",0)
        return (tot-free)//1024, tot//1024, (st-sf)//1024, st//1024
    except: return 0,0,0,0

def nvme_temp():
    t = run("smartctl -A /dev/nvme0 2>/dev/null | awk '/Temperature:/{print $2; exit}'")
    if not t:
        import glob
        pp = glob.glob("/sys/class/nvme/nvme0/hwmon*/temp1_input")
        if pp:
            try: t = str(int(open(pp[0]).read().strip()) // 1000)
            except: t = "---"
    return t or "---"

def disk_usage():
    raw = run("df -h / | awk 'NR==2{print $3,$2,$5}'").replace("%","")
    p = raw.split()
    return (p[0], p[1], int(p[2])) if len(p)==3 else ("---","---",0)

def disk_io():
    def sectors(dev="nvme0n1"):
        try:
            for line in open("/proc/diskstats"):
                p = line.split()
                if len(p) >= 10 and p[2] == dev:
                    # p[5]=sectors_read, p[9]=sectors_written (512 bytes each)
                    return int(p[5]), int(p[9])
        except: pass
        return 0, 0
    r1, w1 = sectors()
    time.sleep(1.0)
    r2, w2 = sectors()
    elapsed = 1.0
    r_mb = round((r2 - r1) * 512 / 1024 / 1024 / elapsed, 1)
    w_mb = round((w2 - w1) * 512 / 1024 / 1024 / elapsed, 1)
    return r_mb, w_mb

def throttle():
    raw = run("vcgencmd get_throttled").replace("throttled=","")
    try:    val = int(raw, 16)
    except: return raw, [f"{DIM}unknown{RESET}"]
    flags = []
    if val & 0x1:     flags.append(f"{RED}Under-voltage{RESET}")
    if val & 0x2:     flags.append(f"{YELLOW}Freq-capped{RESET}")
    if val & 0x4:     flags.append(f"{RED}Throttled{RESET}")
    if val & 0x10000: flags.append(f"{YELLOW}Past under-volt{RESET}")
    if val & 0x40000: flags.append(f"{YELLOW}Past throttle{RESET}")
    if not flags:     flags.append(f"{GREEN}None{RESET}")
    return raw, flags

def voltage():   return run("vcgencmd measure_volts core").replace("volt=","") or "---"

def load_avg():
    try:
        p = open("/proc/loadavg").read().split()
        return float(p[0]), float(p[1]), float(p[2])
    except: return 0.0, 0.0, 0.0

def top_procs(n=5):
    raw = run(f"ps aux --sort=-%cpu | awk 'NR>1&&NR<={n+1}{{printf \"%s %s\\n\",$3,$11}}'")
    return raw.splitlines() if raw else []

def uptime_str(): return run("uptime -p") or run("uptime")
def kernel():     return run("uname -r")

# ── Main snapshot ─────────────────────────────────────────────────────────────

def main():
    w = term_width()

    hr("═", CYAN)
    title = f"{BOLD}{WHITE}  Raspberry Pi 5 — System Monitor{RESET}"
    ts    = time.strftime("%a %d %b %Y  %H:%M:%S")
    gap   = w - vlen(title) - len(ts)
    print(f"{title}{' ' * max(gap,1)}{ts}")
    print(f"  {DIM}Kernel: {kernel()}   Uptime: {uptime_str()}{RESET}")
    hr("═", CYAN)

    print(f"\n  {DIM}{'METRIC':<{C_LBL}}  {'VALUE':<{C_VAL}}  {'ZONE':<{C_ZNE}}  BAR{RESET}")
    print(f"  {'─'*C_LBL}  {'─'*C_VAL}  {'─'*C_ZNE}  {'─'*30}")

    # ── CPU ──────────────────────────────────────────────────
    zone_header("CPU  —  ARM Cortex-A76 × 4", BLUE)
    ct = cpu_temp(); cf = cpu_freq_mhz(); cu = cpu_usage()
    tc = temp_color(ct); tz = temp_zone(ct)
    row("Temperature", f"{tc}{ct:.1f} °C{RESET}", f"{tc}{tz}{RESET}", bar(ct, unit=" (°C/100)"))
    row("Utilisation", f"{use_color(cu)}{cu}%{RESET}", bar_str=bar(cu))
    row2("Frequency",  f"{WHITE}{cf} MHz{RESET}")
    ct2 = core_temps()
    if ct2:
        row2("Per-core", f"{DIM}{'  '.join(f'Core{i}:{t:.0f}°C' for i,t in ct2)}{RESET}")
    hr()

    # ── GPU ──────────────────────────────────────────────────
    zone_header("GPU  —  VideoCore VII", MAGENTA)
    gt = gpu_temp(); gf = gpu_freq_mhz()
    gtc = temp_color(gt); gtz = temp_zone(gt)
    row("Temperature", f"{gtc}{gt:.1f} °C{RESET}", f"{gtc}{gtz}{RESET}", bar(gt, unit=" (°C/100)"))
    row2("Frequency",  f"{WHITE}{gf} MHz{RESET}")
    hr()

    # ── Hailo-8 — same Device() pattern as hailo_temp.sh ─────
    zone_header("Hailo-8  —  AI Accelerator  (26 TOPS)", YELLOW)
    h_svc  = hailo_service()
    h_drv  = hailo_driver()
    h_hmon = hailo_hmon()
    h_dot  = dot("ok") if h_svc == "active" else dot("warn")
    ht, h_err = hailo_temp()

    if ht is not None:
        hc  = get_hailo_color(ht)
        htz = get_hailo_zone(ht)
        hbar = format_hailo_bar(ht)
        row("Temperature",
            f"{hc}{ht:>7.2f}°C{RESET}",
            f"{hc}{htz}{RESET}",
            hbar)
    else:
        row2("Temperature", f"{RED}ERR{RESET}  {DIM}{h_err}{RESET}")

    row2("Service",    f"{h_dot} {WHITE}{h_svc}{RESET}")
    row2("Driver",     f"{WHITE}{h_drv}{RESET}")
    row2("hmon files", f"{WHITE}{h_hmon} active{RESET}")
    hr()

    # ── Memory ───────────────────────────────────────────────
    zone_header("Memory  —  LPDDR4X", GREEN)
    mu, mt, su, st = memory_mb()
    mp = int(mu*100/mt) if mt>0 else 0
    sp = int(su*100/st) if st>0 else 0
    row("RAM",  f"{use_color(mp)}{mu} / {mt} MB{RESET}",  bar_str=bar(mp))
    row("Swap", f"{use_color(sp)}{su} / {st} MB{RESET}", bar_str=bar(sp))
    hr()

    # ── NVMe ─────────────────────────────────────────────────
    zone_header("NVMe  —  Storage", CYAN)
    nt = nvme_temp()
    ntc = temp_color(nt) if nt != "---" else DIM
    ntz = temp_zone(nt)  if nt != "---" else "N/A"
    du, dt, dpct = disk_usage(); rio, wio = disk_io()
    nt_disp = f"{ntc}{float(nt):.1f} °C{RESET}" if nt != "---" else f"{DIM}---{RESET}"
    row("Temperature", nt_disp, f"{ntc}{ntz}{RESET}", bar(dpct, unit=" (disk)"))
    row("Disk usage",  f"{WHITE}{du} / {dt}{RESET}", bar_str=bar(dpct))
    row2("Throughput", f"{WHITE}R:{rio} MB/s  W:{wio} MB/s{RESET}")
    hr()

    # ── Power ────────────────────────────────────────────────
    zone_header("Power  &  Throttle", RED)
    vlt = voltage(); traw, flags = throttle()
    row2("Core voltage",    f"{WHITE}{vlt}{RESET}")
    row2("Throttled (raw)", f"{WHITE}{traw}{RESET}")
    row2("Flags",           "  ".join(flags))
    hr()

    # ── Load ─────────────────────────────────────────────────
    zone_header("System Load  &  Processes", WHITE)
    l1, l5, l15 = load_avg()
    l1p  = min(int(l1  / 4 * 100), 100)
    l5p  = min(int(l5  / 4 * 100), 100)
    l15p = min(int(l15 / 4 * 100), 100)
    row("Load  1m",  f"{use_color(l1p)}{l1:.2f}{RESET}",  bar_str=bar(l1p))
    row("Load  5m",  f"{use_color(l5p)}{l5:.2f}{RESET}",  bar_str=bar(l5p))
    row("Load 15m",  f"{use_color(l15p)}{l15:.2f}{RESET}", bar_str=bar(l15p))
    procs = top_procs()
    if procs:
        print()
        print(f"  {DIM}Top processes by CPU:{RESET}")
        print(f"  {DIM}  {'CPU%':>6}  {'Command'}{RESET}")
        print(f"  {DIM}  {'─'*6}  {'─'*40}{RESET}")
        for p in procs:
            parts = p.split(None, 1)
            if len(parts) == 2:
                cpu_p, cmd = parts
                c = use_color(int(float(cpu_p)))
                print(f"    {c}{float(cpu_p):6.1f}%{RESET}  {DIM}{cmd[:60]}{RESET}")

    hr("═", CYAN)

if __name__ == "__main__":
    main()
PYEOF

chmod +x "$SCRIPT"

lxterminal \
    --title="Pi5 System Monitor" \
    --geometry=120x55 \
    -e bash -c "watch -n 1 -c python3 '$SCRIPT'; exec bash"
