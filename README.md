## References:
- https://github.com/r00tman/maschine-mikro-mk3-driver/tree/main
- https://github.com/wrl/maschine.rs/blob/trunk/src/main.rs
- https://github.com/roman-galyaminskiy/korg-volca-sample/blob/develop/main/voice.hpp

Get-WmiObject -Query "SELECT * FROM Win32_PnPEntity WHERE Name like 'DORE%'"
$pid1 = "1700"  # Product ID
$vid1 = "17CC"  # Vendor ID
Get-WmiObject -Query "SELECT * FROM Win32_PnPEntity WHERE DeviceID LIKE 'USB\\VID_$vid1&PID_$pid1%'"