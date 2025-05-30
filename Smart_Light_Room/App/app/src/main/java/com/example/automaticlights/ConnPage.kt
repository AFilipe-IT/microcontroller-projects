package com.example.automaticlights

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.automaticlights.ui.theme.AutomaticLightsTheme
import org.eclipse.paho.client.mqttv3.MqttClient

@Composable
fun ConnScreen(client: HelperMQTT, onSuccess: () -> Unit) {
    var button by remember { mutableStateOf(true) }
    var field1 by remember { mutableStateOf(MqttClient.generateClientId()) }
    var field2 by remember { mutableStateOf("tcp://mqtt-dashboard.com:1883") }
    val toast = Toast.makeText(LocalContext.current, "Connection failed!", Toast.LENGTH_SHORT)
    val context = LocalContext.current.applicationContext
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = {
            Text(text = "Broker MQTT connection:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Client ID")
            OutlinedTextField(
                value = field1,
                onValueChange = { field1 = it },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Server URI")
            OutlinedTextField(
                value = field2,
                onValueChange = { field2 = it },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                enabled = button,
                content = { Text(text = "Connect") },
                colors = ButtonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    disabledContainerColor = Color.DarkGray,
                    disabledContentColor = Color.White
                ),
                onClick = {
                    button = false
                    client.setParams(context, field2, field1)
                    client.test { success ->
                        if (success) onSuccess()
                        else toast.show()
                        button = true
                    }
                }
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun QuickConnPreview() {
    AutomaticLightsTheme { ConnScreen(HelperMQTT()) {} }
}
