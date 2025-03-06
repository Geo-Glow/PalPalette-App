package com.app.geoglow.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.app.geoglow.R
import com.app.geoglow.SendColorsResult
import com.app.geoglow.data.model.Message
import com.app.geoglow.network.client.RestClient
import com.app.geoglow.utils.general.formatTimestamp
import com.app.geoglow.utils.storage.SharedPreferencesHelper
import com.app.geoglow.viewmodel.MessageViewModel
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

data class FilterState(
    val last24hours: Boolean,
    val lastWeek: Boolean,
    val customPeriod: Boolean,
    val unseenOnly: Boolean,
    val startDate: Long,
    val endDate: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    navController: NavController,
    friendId: String,
    messageViewModel: MessageViewModel
) {
    val context = LocalContext.current
    val groupId by messageViewModel.groupId.collectAsState()
    val restClient = remember { RestClient(context) }
    val prefsHelper = remember { SharedPreferencesHelper }

    var messages by remember { mutableStateOf(emptyList<Message>()) }
    var isLoading by remember { mutableStateOf(true) }

    var filterState by remember {
        mutableStateOf(
            FilterState(
                last24hours = false,
                lastWeek = false,
                customPeriod = false,
                unseenOnly = prefsHelper.getUnseenOnlyPreference(context),
                startDate = System.currentTimeMillis(),
                endDate = System.currentTimeMillis()
            )
        )
    }

    var friends by remember { mutableStateOf<Map<String, String>?>(null) }
    var selectedFriend by remember { mutableStateOf<String?>(null) }

    fun fetchMessages(selectedFriendId: String? = null) {
        isLoading = true

        val callback: (List<Message>?, Throwable?) -> Unit = { fetchedMessages, error ->
            if (fetchedMessages != null) {
                val filteredMessages = if (selectedFriendId != null) {
                    fetchedMessages.filter { it.fromFriendId == selectedFriendId }
                } else {
                    fetchedMessages
                }

                val seenMessages = prefsHelper.getSeenMessages(context)
                messages = if (filterState.unseenOnly) {
                    filteredMessages.filter { it.id !in seenMessages }.toList()
                } else {
                    filteredMessages.toList()
                }
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_fetching_messages, error), Toast.LENGTH_SHORT
                ).show()
            }
            isLoading = false
        }

        when {
            filterState.last24hours -> {
                val calendar = Calendar.getInstance().apply {
                    timeZone = TimeZone.getDefault()
                    add(Calendar.DAY_OF_YEAR, -1)
                }

                val startTime = calendar.timeInMillis
                val endTime = System.currentTimeMillis()
                restClient.getMessagesWithTimeFrame(friendId, null, startTime, endTime, callback)
            }

            filterState.lastWeek -> {
                val calendar = Calendar.getInstance().apply {
                    timeZone = TimeZone.getDefault()
                    add(Calendar.WEEK_OF_YEAR, -1)
                }

                val startTime = calendar.timeInMillis
                val endTime = System.currentTimeMillis()
                restClient.getMessagesWithTimeFrame(friendId, null, startTime, endTime, callback)
            }

            filterState.customPeriod -> {
                restClient.getMessagesWithTimeFrame(friendId, null, filterState.startDate, filterState.endDate, callback)
            }

            else -> {
                restClient.getMessages(friendId, null, callback)
            }
        }
    }

    restClient.getAllFriends(groupId, onResult = { friendList, _ ->
        friends = friendList?.associate { it.friendId to it.name }
    })

    LaunchedEffect(friendId, filterState, selectedFriend) {
        fetchMessages(selectedFriend)
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
            MessageFilter(
                friendNames = friends?.values?.toList() ?: listOf(),
                filterState = filterState,
                selectedFriend = selectedFriend,
                onFilterChange = { friendName, start, end ->
                    selectedFriend = friends?.entries?.firstOrNull { it.value == friendName }?.key
                },
                onQuickFilterChange = { newState ->
                    filterState = newState
                }
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
                            Toast.makeText(
                                context,
                                context.getString(R.string.colors_sent_successfully),
                                Toast.LENGTH_SHORT
                            )
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

@Composable
fun FilterSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    OutlinedCard(
        onClick = { onCheckedChange(!checked) },
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
            )
        }
    }
}

@Composable
fun MessageList(
    friends: Map<String, String>?,
    messages: List<Message>,
    onMessageClick: (Message) -> Unit
) {
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

    val formattedTime = formatTimestamp(timestamp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = fromFriend,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageItemPreview() {
    val fromFriend = "Nick"
    val timestamp = Date()
    MessageItem(fromFriend = fromFriend, timestamp = timestamp) {
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenu(
    selectedItem: String,
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(selectedItem) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            readOnly = true,
            value = selectedOptionText,
            onValueChange = { },
            label = { Text("Select Friend") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { selectionOption ->
                DropdownMenuItem(
                    text = {
                        Text(text = selectionOption)
                    },
                    onClick = {
                        selectedOptionText = selectionOption
                        expanded = false
                        onItemSelected(selectionOption)
                    }
                )
            }
        }
    }
}

@Composable
fun MessageFilter(
    friendNames: List<String>,
    filterState: FilterState,
    selectedFriend: String?,
    onFilterChange: (String, Long?, Long?) -> Unit,
    onQuickFilterChange: (FilterState) -> Unit
) {
    val friendList = listOf("None") + friendNames
    var localSelectedFriend by remember { mutableStateOf(selectedFriend ?: "None") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Filter Messages",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))
        // Friend selection dropdown menu
        DropdownMenu(localSelectedFriend, friendList) { friend ->
            localSelectedFriend = friend
            onFilterChange(friend, null, null)
        }

        // Quick Filters
        Spacer(modifier = Modifier.height(16.dp))

        FilterSwitch(
            label = stringResource(R.string.last_24_hours),
            checked = filterState.last24hours,
            onCheckedChange = { isChecked ->
                onQuickFilterChange(
                    filterState.copy(
                        last24hours = isChecked,
                        lastWeek = false,
                        customPeriod = false
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        FilterSwitch(
            label = stringResource(R.string.last_week),
            checked = filterState.lastWeek,
            onCheckedChange = { isChecked ->
                onQuickFilterChange(
                    filterState.copy(
                        lastWeek = isChecked,
                        last24hours = false,
                        customPeriod = false
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        FilterSwitch(
            label = stringResource(R.string.unseen_only),
            checked = filterState.unseenOnly,
            onCheckedChange = { isChecked ->
                onQuickFilterChange(filterState.copy(unseenOnly = isChecked))
            }
        )
    }
}