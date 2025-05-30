// Smart Room Lighting System
// Hardware: Wemos D1 R1, Reed switch (door sensor), LDR (light sensor), PIR, LED
// Functionality: Detect users by MAC, control LED, interact with MQTT broker via AP mode

#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>
#include <WiFiClient.h>
#include <PubSubClient.h>

// WiFi and MQTT Settings
const char* ap_ssid = "SmartRoom_AP";
const char* ap_password = "password1234";
const IPAddress ap_local_ip(192, 168, 4, 1);
const IPAddress ap_gateway(192, 168, 4, 1);
const IPAddress ap_subnet(255, 255, 255, 0);

const char* mqtt_broker_ip = "192.168.4.2"; // IP do Raspberry Pi dentro da rede do AP do ESP
const int mqtt_port = 1883;

WiFiServer server(80);
WiFiClient espClient;
PubSubClient client(espClient);

// MQTT Topics
const char* topic_light_set = "room/light/set";
const char* topic_light_status = "room/light/status";
const char* topic_welcome = "room/welcome";
const char* topic_warnings = "room/warnings";
const char* topic_name = "room/name";

// Pins
#define LED_PIN        D1
#define DOOR_SENSOR    D2
#define LDR_PIN        A0
#define PIR_SENSOR     D5

// Thresholds
#define LIGHT_THRESHOLD 600  // LDR value threshold (adjust experimentally)

// States
bool lightOn = false;
bool doorOpen = false;
bool userInside = false;
String userName = "User";
unsigned long lastDetection = 0;

// Known MAC addresses
const char* knownUsers[] = {
  "0A:FE:65:CA:80:7B", // Example MAC address
  "7B:80:CB:03:35:32"
};

// Setup AP and connect to MQTT broker in Raspberry Pi
void setupWiFi() {
  WiFi.mode(WIFI_AP_STA);
  WiFi.softAPConfig(ap_local_ip, ap_gateway, ap_subnet);
  WiFi.softAP(ap_ssid, ap_password);
  delay(100);
  WiFi.begin(ap_ssid, ap_password); // STA tenta conectar à própria rede
}

void reconnectMQTT() {
  while (!client.connected()) {
    if (client.connect("SmartRoomController")) {
      client.subscribe(topic_light_set);
      client.subscribe(topic_name);
    } else {
      delay(1000);
    }
  }
}

void callback(char* topic, byte* payload, unsigned int length) {
  String message;
  for (unsigned int i = 0; i < length; i++) message += (char)payload[i];

  if (String(topic) == topic_light_set) {
    if (message == "1") {
      if (!userInside && analogRead(LDR_PIN) > LIGHT_THRESHOLD) {
        client.publish(topic_warnings, "Luminosidade suficiente. Luz não ligada.");
        return;
      }
      lightOn = true;
      digitalWrite(LED_PIN, HIGH);
      client.publish(topic_light_status, "on");
    } else if (message == "0") {
      if (userInside) return; // Ignore if someone is inside
      lightOn = false;
      digitalWrite(LED_PIN, LOW);
      client.publish(topic_light_status, "off");
    }
  }

  if (String(topic) == topic_name) {
    userName = message;
  }
}

bool isKnownMAC(const String& mac) {
  for (const char* addr : knownUsers) {
    if (mac.equalsIgnoreCase(String(addr))) return true;
  }
  return false;
}

void scanForKnownDevices() {
  int n = WiFi.scanNetworks();
  for (int i = 0; i < n; ++i) {
    String mac = WiFi.BSSIDstr(i);
    if (isKnownMAC(mac)) {
      lastDetection = millis();
      return;
    }
  }
}

void setup() {
  pinMode(LED_PIN, OUTPUT);
  pinMode(DOOR_SENSOR, INPUT_PULLUP);
  pinMode(PIR_SENSOR, INPUT);

  setupWiFi();
  client.setServer(mqtt_broker_ip, mqtt_port);
  client.setCallback(callback);
}

void loop() {
  if (!client.connected()) reconnectMQTT();
  client.loop();

  // Detect door state
  bool currentDoorState = digitalRead(DOOR_SENSOR) == LOW;
  if (currentDoorState && !doorOpen) {
    doorOpen = true;
    scanForKnownDevices();

    if ((millis() - lastDetection) < 5000) {
      if (analogRead(LDR_PIN) < LIGHT_THRESHOLD) {
        lightOn = true;
        digitalWrite(LED_PIN, HIGH);
        client.publish(topic_light_status, "on");
      }
      client.publish(topic_welcome, ("Bem-vindo, " + userName + "!").c_str());
      userInside = true;
    }
  } else if (!currentDoorState && doorOpen) {
    doorOpen = false;
    delay(500); // Ensure PIR is triggered after exit
    if (!digitalRead(PIR_SENSOR)) {
      userInside = false;
      lightOn = false;
      digitalWrite(LED_PIN, LOW);
      client.publish(topic_light_status, "off");
    }
  }

  // Periodic user scan (every 5s max)
  static unsigned long lastScan = 0;
  if (millis() - lastScan > 5000) {
    scanForKnownDevices();
    lastScan = millis();
  }
}
