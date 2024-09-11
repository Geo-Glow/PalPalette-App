package com.example.geoglow.ui.screen

import android.Manifest
import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.geoglow.CustomGalleryContract
import com.example.geoglow.R
import com.example.geoglow.ui.navigation.Screen
import com.example.geoglow.utils.general.createImageFile
import com.example.geoglow.utils.permission.PermissionHandler
import com.example.geoglow.utils.storage.DataStoreManager
import com.example.geoglow.viewmodel.ColorViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

    val galleryLauncher =
        rememberLauncherForActivityResult(contract = CustomGalleryContract()) { uri ->
            if (uri != null) {
                uri.let(viewModel::setColorState)
                navController.navigate(Screen.ImageScreen.route)
            } else {
                Log.e("Composables", "no picture chosen")
            }
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                viewModel.setColorState(imageUri, true)
                navController.navigate(Screen.ImageScreen.route)
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
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "GeoGlow",
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
                        text = "Take photo",
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
                        text = "Choose image",
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
                showFriendIDDialog = false
                scope.launch {
                    dataStoreManager.storeFriendID(id)
                    friendID = id
                }
            }
        )
    }
}

@Composable
fun EnterFriendIDDialog(onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = "Enter your FriendID") },
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
                Text("OK")
            }
        },
        dismissButton = {
            Button(
                onClick = { /* Handle dismiss action if needed */ }
            ) {
                Text("Cancel")
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
                Text("Reset FriendID")
            }
        }
    )
}