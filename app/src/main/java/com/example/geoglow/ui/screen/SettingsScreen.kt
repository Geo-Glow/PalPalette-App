package com.example.geoglow.ui.screen

import android.app.TimePickerDialog
import android.util.Log
import android.widget.TimePicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.geoglow.R
import com.example.geoglow.network.client.RestClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, friendId: String, restClient: RestClient) {
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("10:00") }

    var selectedTime: TimePickerState? by remember { mutableStateOf(null) }

    var isStartTimeDialogOpen by remember { mutableStateOf(false) }
    var isEndTimeDialogOpen by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Set Availability",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Start Time Picker
            TextButton(onClick = { isStartTimeDialogOpen = true }) {
                Text("Start Time: $startTime")
            }
            if (isStartTimeDialogOpen) {
                TimePickerDialog(
                    initialTime = startTime,
                    onConfirm = { time ->
                        selectedTime = time
                        startTime = "%02d:%02d".format(selectedTime!!.hour, selectedTime!!.minute)
                        isStartTimeDialogOpen = false
                    },
                    onDismiss = { isStartTimeDialogOpen = false }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // End Time Picker
            TextButton(onClick = { isEndTimeDialogOpen = true }) {
                Text("End Time: $endTime")
            }
            if (isEndTimeDialogOpen) {
                TimePickerDialog(
                    initialTime = endTime,
                    onConfirm = { time ->
                        selectedTime = time
                        endTime = "%02d:%02d".format(selectedTime!!.hour, selectedTime!!.minute)
                        isEndTimeDialogOpen = false
                    },
                    onDismiss = { isEndTimeDialogOpen = false }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                restClient.sendTimeoutTimes(friendId, startTime, endTime, onResult = { result, error ->
                    Log.d("SettingsScreen", "Result: $result, Error: $error")
                })
                navController.navigateUp()
            }) {
                Text("Save Settings")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: String,
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialHour = initialTime.split(":")[0].toInt()
    val initialMinute = initialTime.split(":")[1].toInt()

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    Column {
        TimePicker(
            state = timePickerState,
        )
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Dismiss picker")
        }
        Button(
            onClick = { onConfirm(timePickerState) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Confirm selection")
        }
    }
}