# 💡 Smart Room Lighting System

An intelligent lighting system that automatically turns lights on or off based on door status, user presence, and ambient light, with real-time control and feedback via MQTT and an Android app.

## 📦 Project Structure

```text
smart-room-lighting/
├── arduino/
│   └── smart_room.ino           # Full Wemos D1 (ESP8266) firmware
├── raspberry/
│   └── setup_mqtt_broker.sh     # Script to configure Raspberry Pi as MQTT broker
├── android-app/
│   └── [code or APK link]
├── docs/
│   ├── diagrama_esquematico.png # Sensor and ESP connection diagram
│   ├── fluxo_funcional.drawio   # Logical flow diagram
│   └── arquitetura_rede.png     # Network architecture diagram
├── README.md                    # Project documentation
├── LICENSE                      # License (MIT, GPL, etc.)
└── .gitignore                   # Git version control exclusions
```
## 🧠 Overview

- **Automatically controls** a light (LED) when a known user enters or leaves a room.
- **Uses sensors**: door (reed switch), motion (PIR), light (LDR).
- **Detects user devices** by MAC address.
- **Controls and monitors** light status via MQTT messages.
- **Displays welcome messages** per user via MQTT.
- Operates **offline**: ESP8266 runs a local Wi-Fi Access Point and connects to a Raspberry Pi broker.

## ⚙️ Features

- Light turns **on** when:
  - Door opens.
  - A known user is detected (via MAC).
  - Ambient light is below threshold.
- Light turns **off** when:
  - Door closes **and**
  - No motion detected inside the room.
- Light can be manually controlled via Android app, respecting presence detection.
- Real-time light status and warnings (e.g., “ambient light sufficient”) are published.
- Personalized welcome message on user entry.
- ESP8266 acts as Wi-Fi AP for fully local communication.

## 📡 MQTT Topics

| Topic               | Direction       | Payload           | Description                                |
|---------------------|------------------|--------------------|--------------------------------------------|
| `room/light/set`    | App → ESP        | `"1"` / `"0"`      | Turn light on/off                          |
| `room/light/status` | ESP → App        | `"on"` / `"off"`   | Current light status                       |
| `room/welcome`      | ESP → App        | `"Welcome, Ana!"`  | Personalized message on room entry         |
| `room/name`         | App → ESP        | `"Ana"`            | User-defined name for welcome message      |
| `room/warnings`     | ESP → App        | Warning text       | E.g., ambient light too high to turn light |

## ⏱️ Timing Requirements

| Requirement                         | Maximum Delay |
|-------------------------------------|----------------|
| User detection                      | ≤ 5 seconds    |
| Remote light control (MQTT)         | ≤ 4 seconds    |
| Light status update on app          | ≤ 4 seconds    |

## 🧰 Hardware Requirements

- Wemos D1 R1 (ESP8266)
- Raspberry Pi (any version with Wi-Fi)
- Reed switch (door sensor)
- PIR motion sensor
- LDR + resistor (light sensor)
- LED + resistor
- Android phone with MQTT-compatible app

## 🔧 Installation & Usage

### 1. Flash the ESP8266

- Upload `arduino/smart_room.ino` using the Arduino IDE.
- The ESP will create a Wi-Fi network (`SmartRoom_AP`).

### 2. Set up the MQTT Broker (Raspberry Pi)

- Connect Raspberry Pi to the ESP’s Wi-Fi network.
- Run the script `raspberry/setup_mqtt_broker.sh` to:
  - Set static IP (192.168.4.2)
  - Install Mosquitto broker
  - Enable and start the service

### 3. Android App

- Connect the phone to `SmartRoom_AP`.
- Use the Android app to:
  - Set your name (`room/name`)
  - Control light (`room/light/set`)
  - Receive light status and welcome messages

## 🖼️ Documentation

- `docs/diagrama_esquematico.png`: Pinout and sensor wiring
- `docs/fluxo_funcional.drawio`: Logical behavior flowchart
- `docs/arquitetura_rede.png`: Network topology (ESP8266 AP → Raspberry Pi + Android)

## 📝 License

MIT License — See [LICENSE](LICENSE) file for full terms.

---

### 🚀 Future Improvements

- User detection via BLE or Wi-Fi triangulation
- Add web dashboard
- Support multiple rooms or lights
- Energy consumption tracking

---

Contributions and feedback are welcome!
