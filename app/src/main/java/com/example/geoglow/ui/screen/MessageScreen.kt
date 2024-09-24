package com.example.geoglow.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.geoglow.R
import com.example.geoglow.SendColorsResult
import com.example.geoglow.data.model.Message
import com.example.geoglow.network.client.RestClient
import com.example.geoglow.utils.general.extractFriendName
import com.example.geoglow.utils.general.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(navController: NavController, friendId: String) {
    val context = LocalContext.current
    val restClient = remember { RestClient(context) }

    var messages by remember { mutableStateOf(emptyList<Message>())}
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(friendId) {
        restClient.getMessages(toFriendId = friendId, fromFriendId = null) { fetchedMessages, error ->
            if (fetchedMessages != null) {
                messages = fetchedMessages
            } else {
                Toast.makeText(context, "Error fetching messages: $error", Toast.LENGTH_SHORT).show()
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            messages.forEach { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(onClick = {
                            restClient.sendColors(message.toFriendId, message.fromFriendId, message.colors, false) { result, error ->
                                if (result == SendColorsResult.SUCCESS) {
                                    Toast.makeText(context, "Message sent successfully", Toast.LENGTH_SHORT).show()
                                }
                        navController.popBackStack()
                            }
                        }),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(text = "${stringResource(R.string.message_from)}: ${extractFriendName(message.fromFriendId)}")
                        Text(text = "${stringResource(R.string.message_time)}: ${formatTimestamp(message.timestamp)}")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}