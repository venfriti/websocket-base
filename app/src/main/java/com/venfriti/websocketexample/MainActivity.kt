package com.venfriti.websocketexample

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.venfriti.websocketexample.ui.theme.WebsocketExampleTheme
import com.venfriti.websocketexample.ui.theme.dirtyWhite
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
                var name by remember { mutableStateOf("") }
                var message by remember { mutableStateOf("") }
                var receivedMessage by remember { mutableStateOf("") }
                val scope = rememberCoroutineScope()

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
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
                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Sender") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { sendMessage(name, message) }) {
                            Text("Send Message")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("$receivedMessage")
                    }
                }
                LaunchedEffect(Unit) {
                    initSocketIO { name, receivedText ->
                        scope.launch(Dispatchers.Main) {
                            receivedMessage = "$name: $receivedText"
                        }
                    }
                }
            }
        }
        }

    private fun initSocketIO(onMessageReceived: (String, String) -> Unit) {
        try {
            socket = IO.socket("http://192.168.0.101:5000")

            socket.on(Socket.EVENT_CONNECT) {
                println("Socket.IO Connected")
            }

            socket.on("message") { args ->
                if (args.isNotEmpty()) {
                    val msg = args[0] as? JSONObject
                    if (msg != null) {
                        val sender = msg.getString("name")
                        val text = msg.getString("message")
                        onMessageReceived(sender, text)
                    } else {
                        println("Received unknown message type: $args")
                    }
//                    // Check if the message is already a String
//                    if (msg is String) {
//                        // If it's a JSON string, you can optionally parse it to JSONObject
//                        try {
//                            val jsonObject = JSONObject(msg)
//                            val text = jsonObject.getString("text") // Assuming the JSON contains a "text" field
//                            onMessageReceived(text)
//                        } catch (e: Exception) {
//                            // If it's not a JSON string, just handle it as a plain text message
//                            onMessageReceived(msg)
//                        }
//                    } else {
//                        // Handle other types if necessary
//                        println("Received unknown message type: $msg")
//                    }
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

    private fun sendMessage(name: String, message: String) {
        if (socket.connected()) {
            val jsonObject = JSONObject()
            jsonObject.put("name", name)
            jsonObject.put("message", message)
            socket.emit("message", jsonObject)
        }
    }
}
