# Data packet info

All packets in JavaHomeServer are sent and received via UDP protocol.

In order to filter UDP packets, data packet should have a `WELCOME BYTE` in the first byte, which is `0x52`.

```
    data_packet[0] = 0x52
```

All following data should be represented as UTF-8 hex.

The next data portion is the device type, which should be equal to the names in the [SmartHomeDevice's Device folder]()

```
    data_packet[1..7] = "LightDevice"
```

For delimiter use a semicolon `;`

```
    data_packet[8] = ";"
```

Next is required device data: `id` (int), `location` (string), `on` (boolean). Assign value with equals sign `=`

```
    data_packet[9...] = "id=123;location=locationString;on=true"
```

Depending on Device type it may have more data, such as `brightness`, `green`, `temperature`, etc. See [SmartHomeDevices]() for more information about certain Devices.
