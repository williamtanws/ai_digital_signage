### Configure EEPROM for Stable Power and Boot Behavior

Run the following command to edit the Raspberry Pi EEPROM configuration:

```bash
sudo rpi-eeprom-config -e
```

Append or update the configuration with the following values:
```bash
BOOT_UART=1
POWER_OFF_ON_HALT=1
BOOT_ORDER=0xf41
PSU_MAX_CURRENT=5000
```

Save and exit the editor, then reboot the Raspberry Pi to apply the changes:
```bash
sudo reboot
```
