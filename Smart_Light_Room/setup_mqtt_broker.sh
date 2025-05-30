#!/bin/bash

# Configurações
STATIC_IP="192.168.4.2"
INTERFACE="wlan0"  # ou eth0 se for via cabo
GATEWAY="192.168.4.1"
DNS="8.8.8.8"

echo "### Atualizando pacotes..."
apt update && apt upgrade -y

echo "### Instalando Mosquitto (broker MQTT)..."
apt install -y mosquitto mosquitto-clients

echo "### Habilitando e iniciando o Mosquitto..."
systemctl enable mosquitto
systemctl start mosquitto

echo "### Configurando IP estático..."
# Backup do dhcpcd.conf
cp /etc/dhcpcd.conf /etc/dhcpcd.conf.bak

# Adiciona configuração estática ao final do arquivo
cat <<EOF >> /etc/dhcpcd.conf

interface $INTERFACE
static ip_address=$STATIC_IP/24
static routers=$GATEWAY
static domain_name_servers=$DNS
EOF

echo "### Reiniciando serviços de rede..."
systemctl restart dhcpcd

echo "### Verificando IP atribuído..."
ip a show $INTERFACE

echo "### Broker MQTT está disponível em tcp://$STATIC_IP:1883"