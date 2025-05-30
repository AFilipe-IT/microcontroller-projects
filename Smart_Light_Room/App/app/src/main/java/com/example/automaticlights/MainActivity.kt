package com.example.automaticlights

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.automaticlights.ui.theme.AutomaticLightsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AutomaticLightsTheme { MainScaffold() } }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold() {
    var typePage by remember { mutableIntStateOf(0) }
    val clientMQTT by remember { mutableStateOf(HelperMQTT()) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Automatic Lights")
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Lightbulb icon",
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                    )
                },
                colors = topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                contentColor = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                content = {
                    Text(
                        text = "Project of Group 16",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        },
        content = {
            if (typePage == 0)
                ConnScreen(client = clientMQTT, onSuccess = { typePage = 1 })
            else HomeScreen(client = clientMQTT, onFailure = { typePage = 0 })
        }
    )
}

@Preview(showBackground = true)
@Composable
fun QuickMainPreview() {
    AutomaticLightsTheme { MainScaffold() }
}
