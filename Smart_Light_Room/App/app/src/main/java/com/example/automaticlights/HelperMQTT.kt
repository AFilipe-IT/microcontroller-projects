package com.example.automaticlights

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

class HelperMQTT {
    private val logTag = "AndroidMqttClient"
    private lateinit var context: Context
    private lateinit var serverURI: String
    private lateinit var clientId: String
    private lateinit var mqttClient: MqttAndroidClient

    fun setParams(context: Context, serverURI: String, clientId: String) {
        this.context = context
        this.serverURI = serverURI
        this.clientId = clientId
    }

    fun test(onResult: (Boolean) -> Unit) {
        mqttClient = MqttAndroidClient(context, serverURI, clientId)
        val options = MqttConnectOptions()
        options.isAutomaticReconnect = false
        options.isCleanSession = true
        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(logTag, "Connection success")
                    mqttClient.disconnect(null, object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log.d(logTag, "Success on check")
                            mqttClient.unregisterResources()
                            mqttClient.close()
                            onResult(true)
                        }
                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            Log.d(logTag, "Failed to disconnect")
                            mqttClient.unregisterResources()
                            mqttClient.close()
                            onResult(false)
                        }
                    })
                }
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(logTag, "Connection failure")
                    mqttClient.unregisterResources()
                    mqttClient.close()
                    onResult(false)
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun connect(onFailure: () -> Unit, onNewMessage: (String, String) -> Unit) {
        mqttClient = MqttAndroidClient(context, serverURI, clientId)
        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(logTag, "Receive message: ${message.toString()} from topic: $topic")
                if (topic != null && message != null) {
                    onNewMessage(topic, message.toString())
                }
            }
            override fun connectionLost(cause: Throwable?) {
                Log.d(logTag, "Connection lost ${cause.toString()}")
                mqttClient.unregisterResources()
                mqttClient.close()
                onFailure()
            }
            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        })
        val options = MqttConnectOptions()
        options.isAutomaticReconnect = false
        options.isCleanSession = true
        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(logTag, "Connection success")
                    subscribe(onFailure, "room/light/status")
                    subscribe(onFailure, "room/welcome")
                    subscribe(onFailure, "room/warnings")
                }
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(logTag, "Connection failure")
                    mqttClient.unregisterResources()
                    mqttClient.close()
                    onFailure()
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribe(onFailure: () -> Unit, topic: String) {
        try {
            if (!mqttClient.isConnected) return
            mqttClient.subscribe(topic, 1, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(logTag, "Subscribed to $topic")
                }
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(logTag, "Failed to subscribe $topic")
                    mqttClient.unregisterResources()
                    mqttClient.close()
                    onFailure()
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(logTag, "$msg published to $topic")
                }
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(logTag, "Failed to publish $msg to $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

}
