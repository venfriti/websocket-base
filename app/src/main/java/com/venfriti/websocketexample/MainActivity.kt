package com.venfriti.websocketexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.venfriti.websocketexample.ui.theme.WebsocketExampleTheme
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URI

class MainActivity : ComponentActivity() {

    private lateinit var socket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebsocketExampleTheme {
                var message by remember { mutableStateOf("") }
                var receivedMessage by remember { mutableStateOf("") }
                val scope = rememberCoroutineScope()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    TextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { sendMessage(message) }) {
                        Text("Send Message")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(receivedMessage)
                }
                LaunchedEffect(Unit) {
                    initSocketIO { receivedText ->
                        scope.launch(Dispatchers.Main) {
                            receivedMessage = receivedText
                        }
                    }
                }
            }
        }
        }

    private fun initSocketIO(onMessageReceived: (String) -> Unit) {
        try {
            socket = IO.socket("http://192.168.0.101:5000")

            socket.on(Socket.EVENT_CONNECT) {
                println("Socket.IO Connected")
            }

            socket.on("message") { args ->
                if (args.isNotEmpty()) {
                    val msg = args[0]
                    // Check if the message is already a String
                    if (msg is String) {
                        // If it's a JSON string, you can optionally parse it to JSONObject
                        try {
                            val jsonObject = JSONObject(msg)
                            val text = jsonObject.getString("text") // Assuming the JSON contains a "text" field
                            onMessageReceived(text)
                        } catch (e: Exception) {
                            // If it's not a JSON string, just handle it as a plain text message
                            onMessageReceived(msg)
                        }
                    } else {
                        // Handle other types if necessary
                        println("Received unknown message type: $msg")
                    }
                }
            }

            socket.on(Socket.EVENT_DISCONNECT) {
                println("Socket.IO Disconnected")
            }

            socket.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendMessage(message: String) {
        if (socket.connected()) {
            val jsonObject = JSONObject()
            jsonObject.put("text", message)
            socket.emit("message", jsonObject)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WebsocketExampleTheme {
        Greeting("Android")
    }
}