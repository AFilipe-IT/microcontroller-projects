package com.example.automaticlights

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.automaticlights.ui.theme.AutomaticLightsTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(client: HelperMQTT, onFailure: () -> Unit) {
    var lightStatus by remember { mutableStateOf("???") }
    var userName by remember { mutableStateOf("unknown") }
    var tokenId by remember { mutableStateOf("82 72 9F 0B") }
    val context = LocalContext.current
    var waitWarning by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    val targetIcon = when (lightStatus.lowercase()) {
        "on" -> Icons.Filled.Lightbulb
        "off" -> Icons.Outlined.Lightbulb
        else -> Icons.Outlined.Lightbulb
    }
    val targetColor = when (lightStatus.lowercase()) {
        "on" -> MaterialTheme.colorScheme.primary
        "off" -> Color.DarkGray
        else -> Color.Gray
    }
    val targetAlpha = when (lightStatus.lowercase()) {
        "on" -> 1f
        "off" -> 1f
        else -> 0.2f
    }
    val animatedColor by animateColorAsState(targetValue = targetColor, label = "color")
    val animatedAlpha by animateFloatAsState(targetValue = targetAlpha, label = "alpha")

    LaunchedEffect(key1 = client) {
        client.connect(
            onFailure = onFailure,
            onNewMessage = { topic, msg ->
                val builder = NotificationCompat.Builder(context, "1")
                    .setSmallIcon(R.drawable.baseline_lightbulb_circle_24)
                    .setContentTitle("Welcome $userName!")
                    .setContentText("Entrance detected...")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setTimeoutAfter(15000)
                onMessage(topic, msg, { state -> lightStatus = state }, userName, context, scope, snackBarHostState, builder, waitWarning, { waitWarning = false})
            }
        )
        createNotificationChannel(context.applicationContext)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = {
            Text(text = "Light information:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Status: $lightStatus")
            Icon(
                imageVector = targetIcon,
                contentDescription = "Lightbulb status",
                tint = animatedColor,
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer(alpha = animatedAlpha)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                content = { Text(text = "Switch") },
                colors = ButtonColors(
                    containerColor = Color.DarkGray,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Black,
                    disabledContentColor = Color.White
                ),
                onClick = {
                    if (lightStatus.lowercase() == "on") {
                        waitWarning = true
                        client.publish("room/light/set", "off")
                    }
                    else if (lightStatus.lowercase() == "off") {
                        waitWarning = true
                        client.publish("room/light/set", "on")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            SnackbarHost(hostState = snackBarHostState)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Configure user:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Name / Token:")
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                singleLine = true,
                modifier = Modifier.width(150.dp).height(50.dp)
            )
            OutlinedTextField(
                value = tokenId,
                onValueChange = { tokenId = it },
                singleLine = true,
                modifier = Modifier.width(150.dp).height(50.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                content = { Text(text = "Refresh") },
                colors = ButtonColors(
                    containerColor = Color.DarkGray,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Black,
                    disabledContentColor = Color.White
                ),
                onClick = {
                    client.publish("room/name", "$userName/$tokenId")
                }
            )
        }
    )
}

fun onMessage(
    topic: String,
    msg: String,
    light: (String) -> Unit,
    name: String,
    context: Context,
    scope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
    builder: NotificationCompat.Builder,
    waitWarning: Boolean,
    resetWarning: () -> Unit
) {
    when (topic) {
        "room/light/status" -> {
            light(msg)
            resetWarning()
        }
        "room/warnings" -> if (waitWarning) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            resetWarning()
        }
        "room/welcome" -> if (name == msg) {
            scope.launch {
                snackBarHostState.showSnackbar("Welcome $msg!")
            }
            with(NotificationManagerCompat.from(context.applicationContext)) {
                if (Build.VERSION.SDK_INT >= 33) {
                    val permissionCheck = ActivityCompat.checkSelfPermission(
                        context.applicationContext,
                        "android.permission.POST_NOTIFICATIONS"
                    )
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                            context.applicationContext as Activity,
                            arrayOf("android.permission.POST_NOTIFICATIONS"),
                            1001
                        )
                        return@with
                    }
                }
                notify(0, builder.build())
            }
        }
    }
}

private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Welcomes"
        val descriptionText = "Welcome messages channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("1", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

@Preview(showBackground = true)
@Composable
fun QuickHomePreview() {
    AutomaticLightsTheme { HomeScreen(HelperMQTT()) {} }
}
