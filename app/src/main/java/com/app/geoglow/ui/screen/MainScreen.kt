package com.app.geoglow.ui.screen

import android.Manifest
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.app.geoglow.CustomGalleryContract
import com.app.geoglow.network.client.RestClient
import com.app.geoglow.ui.navigation.Screen
import com.app.geoglow.utils.general.createImageFile
import com.app.geoglow.utils.permission.PermissionHandler
import com.app.geoglow.utils.storage.DataStoreManager
import com.app.geoglow.viewmodel.ColorViewModel
import com.app.geoglow.R
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Objects

@Composable
fun MainScreen(navController: NavController, viewModel: ColorViewModel) {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val scope = rememberCoroutineScope()

    var expandInfo: Boolean by remember { mutableStateOf(false) }
    var showFriendIDDialog by remember { mutableStateOf(false) }
    var friendID by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val exists = dataStoreManager.friendIDExists.first()
        if (!exists) {
            showFriendIDDialog = true
        } else {
            friendID = dataStoreManager.friendID.first()
        }
    }

    val permissionHandler = PermissionHandler(context)
    val file = context.createImageFile()
    val imageUri = FileProvider.getUriForFile(
        Objects.requireNonNull(context),
        context.packageName + ".provider",
        file
    )

    val handleImageUri: (Uri) -> Unit = { uri ->
        try {
            // List of Tags that should be extracted from the image metadata
            val desiredTags = listOf(
                ExifInterface.TAG_DATETIME,
            )

            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = inputStream?.let { ExifInterface(it) }

            val jsonMap = mutableMapOf<String, String>()

            desiredTags.forEach { tag ->
                exif?.getAttribute(tag)?.let { value ->
                    jsonMap[tag] = value
                }
            }

            val gson = Gson()
            val jsonString = gson.toJson(jsonMap)

            //val imageFile = File(uri.path!!)

            //viewModel.setColorState(uri, jsonString)
            viewModel.uploadAndSetColorState(uri, jsonString)
            navController.navigate(Screen.ImageScreen.route)
        } catch (e: Exception) {
            Log.e("MainScreen", "Error reading metadata", e)
        }
    }

    val galleryLauncher =
        rememberLauncherForActivityResult(contract = CustomGalleryContract()) { uri ->
            if (uri != null) {
                handleImageUri(uri)
            } else {
                Log.e("Composables", "no picture chosen")
            }
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                handleImageUri(imageUri)
            } else {
                Log.e("Composables", "couldn't take picture")
            }
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { success ->
            permissionHandler.onPermissionResult(Manifest.permission.CAMERA, success)
            if (success) {
                cameraLauncher.launch(imageUri)
            } else {
                Log.e("Composables", "no permission for camera")
            }
        }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (expandInfo) {
            InfoDialog(
                friendID = friendID,
                onDismiss = { expandInfo = false },
                onReset = {
                    expandInfo = false
                    showFriendIDDialog = true
                })
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, end = 10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { expandInfo = !expandInfo }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_info_outline_24),
                    contentDescription = "Info",
                    modifier = Modifier.size(25.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 35.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.geoglow_logo),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (permissionHandler.hasPermission(Manifest.permission.CAMERA)) {
                            cameraLauncher.launch(imageUri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_photo_camera_24),
                        contentDescription = "camera"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.image_take),
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExtendedFloatingActionButton(
                    onClick = {
                        galleryLauncher.launch()
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_image_24),
                        contentDescription = "image"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.image_choose),
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExtendedFloatingActionButton(
                    onClick = {
                        friendID.takeIf { it.isNotEmpty() }?.let { id ->
                            navController.navigate("${Screen.MessageScreen.route}/$id")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_list_alt_24),
                        contentDescription = "messages"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.view_messages),
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExtendedFloatingActionButton(
                    onClick = {
                        friendID.takeIf { it.isNotEmpty() }?.let { id ->
                            navController.navigate("${Screen.SettingsScreen.route}/$id")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_settings_24),
                        contentDescription = "settings"
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.settings),
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showFriendIDDialog) {
        EnterFriendIDDialog(
            onConfirm = { id ->

                scope.launch {
                    RestClient(context).getFriendById(id, onResult = { friend, t ->
                        if (friend == null) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.invalid_id), Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showFriendIDDialog = false
                            runBlocking {
                                dataStoreManager.storeFriendID(id)
                                friendID = id
                                dataStoreManager.storeGroupID(friend.groupId)
                            }
                        }
                    })
                }
            },
            onDismiss = { showFriendIDDialog = false }
        )
    }
}

@Composable
fun EnterFriendIDDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = stringResource(R.string.friendId_enter)) },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("FriendID") }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(text)
                }
            ) {
                Text(text = stringResource(R.string.button_confirm))
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text(stringResource(R.string.button_cancle))
            }
        }
    )
}

@Composable
fun InfoDialog(friendID: String, onDismiss: () -> Unit, onReset: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Info") },
        text = {
            Text(text = "FriendID: $friendID")
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(
                onClick = { onReset() }
            ) {
                Text(text = stringResource(R.string.friendId_reset))
            }
        }
    )
}