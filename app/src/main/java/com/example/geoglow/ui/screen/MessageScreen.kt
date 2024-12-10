package com.example.geoglow.ui.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.geoglow.R
import com.example.geoglow.SendColorsResult
import com.example.geoglow.data.model.Message
import com.example.geoglow.network.client.RestClient
import com.example.geoglow.utils.general.extractFriendName
import com.example.geoglow.utils.general.formatTimestamp
import com.example.geoglow.utils.storage.SharedPreferencesHelper
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(navController: NavController, friendId: String) {
    val context = LocalContext.current
    val restClient = remember { RestClient(context) }
    val prefsHelper = remember { SharedPreferencesHelper }

    var messages by remember { mutableStateOf(emptyList<Message>())}
    var isLoading by remember { mutableStateOf(true) }
    var last24hours by remember { mutableStateOf(false) }
    var lastWeek by remember { mutableStateOf(false) }
    var customPeriod by remember { mutableStateOf(false) }
    var unseenOnly by remember { mutableStateOf(prefsHelper.getUnseenOnlyPreference(context)) }
    var startDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var showCustomDatePicker by remember { mutableStateOf(false) }
    var friends: Map<String, String>? = null
    restClient.getAllFriends("", onResult = { friendList, _ ->
        friends = friendList?.associate { it.friendId to it.name }
    })

    fun fetchMessages() {
        isLoading = true
        val callback: (List<Message>?, Throwable?) -> Unit = { fetchedMessages, error ->
            if (fetchedMessages != null) {
                val seenMessages = prefsHelper.getSeenMessages(context)
                messages = if (unseenOnly) {
                    fetchedMessages.filter { it.id !in seenMessages }
                } else {
                    fetchedMessages
                }
            } else {
                Toast.makeText(context,
                    context.getString(R.string.error_fetching_messages, error), Toast.LENGTH_SHORT).show()
            }
            isLoading = false
        }

        when {
            last24hours -> {
                val startTime = System.currentTimeMillis() - 24 * 60 * 60 * 1000
                val endTime = System.currentTimeMillis()
                restClient.getMessagesWithTimeFrame(friendId, null, startTime, endTime, callback)
            }
            lastWeek -> {
                val startTime = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
                val endTime = System.currentTimeMillis()
                restClient.getMessagesWithTimeFrame(friendId, null, startTime, endTime, callback)
            }
            customPeriod -> {
                restClient.getMessagesWithTimeFrame(friendId, null, startDate, endDate, callback)
            }
            else -> {
                restClient.getMessages(friendId, null, callback)
            }
        }
    }

    LaunchedEffect(friendId, last24hours, lastWeek, customPeriod, unseenOnly, startDate, endDate) {
        fetchMessages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.messages)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            MessageFilters(
                last24hours = last24hours,
                onLast24HoursChange = { it: Boolean ->
                    last24hours = it
                    lastWeek = false
                    customPeriod = false
                },
                lastWeek = lastWeek,
                onLastWeekChange = { it: Boolean ->
                    lastWeek = it
                    last24hours = false
                    customPeriod = false
                },
                customPeriod = customPeriod,
                onCustomPeriodChange = { it: Boolean ->
                    customPeriod = it
                    last24hours = false
                    lastWeek = false
                    if (it) {
                        showCustomDatePicker = true
                    }
                },
                unseenOnly = unseenOnly,
                onUnseenOnlyChange = { it: Boolean ->
                    unseenOnly = it
                    prefsHelper.setUnseenOnlyPreference(context, it)
                },
                onCustomPeriodSelected = { start: Long, end: Long ->
                    startDate = start
                    endDate = end
                },
                initialStartDate = startDate,
                initialEndDate = endDate
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                MessageList(friends = friends, messages = messages, onMessageClick = { message ->
                    prefsHelper.addSeenMessages(context, message.id)
                    restClient.sendColors(
                        toFriendId = message.toFriendId,
                        fromFriendId = message.fromFriendId,
                        colors = message.colors,
                        shouldSaveMessage = false
                    ) { response, error ->
                        if (response == SendColorsResult.SUCCESS) {
                            Toast.makeText(context,
                                context.getString(R.string.colors_sent_successfully), Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.error_sending_colors, error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MessageFilters(
    last24hours: Boolean,
    onLast24HoursChange: (Boolean) -> Unit,
    lastWeek: Boolean,
    onLastWeekChange: (Boolean) -> Unit,
    customPeriod: Boolean,
    onCustomPeriodChange: (Boolean) -> Unit,
    unseenOnly: Boolean,
    onUnseenOnlyChange: (Boolean) -> Unit,
    onCustomPeriodSelected: (Long, Long) -> Unit,
    initialStartDate: Long,
    initialEndDate: Long
) {
    var showCustomDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        FilterSwitch(
            label = stringResource(R.string.last_24_hours),
            checked = last24hours,
            onCheckedChange = onLast24HoursChange
        )
        Spacer(modifier = Modifier.height(8.dp))
        FilterSwitch(
            label = stringResource(R.string.last_week),
            checked = lastWeek,
            onCheckedChange = onLastWeekChange
        )

        Spacer(modifier = Modifier.height(8.dp))
        FilterSwitch(
            label = stringResource(R.string.unseen_only),
            checked = unseenOnly,
            onCheckedChange = onUnseenOnlyChange
        )
    }
}

@Composable
fun FilterSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    OutlinedCard(
        onClick = { onCheckedChange(!checked)},
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun MessageList(friends: Map<String, String>?, messages: List<Message>, onMessageClick: (Message) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages) { message ->
            val friendName = friends?.get(message.fromFriendId)
            if (friendName != null) {
                MessageItem(
                    fromFriend = friendName,
                    timestamp = message.timestamp,
                    onClick = { onMessageClick(message) },
                )
            }
        }
    }
}

@Composable
fun MessageItem(fromFriend: String, timestamp: Date, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "${stringResource(R.string.message_from)}: $fromFriend")
            Text(text = "${stringResource(R.string.message_time)}: ${formatTimestamp(timestamp)}")
        }
    }
}