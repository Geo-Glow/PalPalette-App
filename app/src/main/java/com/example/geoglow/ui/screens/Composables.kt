package com.example.geoglow.ui.screens

import android.Manifest
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.geoglow.ColorViewModel
import com.example.geoglow.CustomGalleryContract
import com.example.geoglow.Friend
import com.example.geoglow.PermissionHandler
import com.example.geoglow.R
import com.example.geoglow.RestClient
import com.example.geoglow.createImageFile
import kotlinx.coroutines.delay
import java.util.Objects

@Composable
fun MainScreen(navController: NavController, viewModel: ColorViewModel, restClient: RestClient) {
    val context = LocalContext.current
    var expandInfo: Boolean by remember { mutableStateOf(false) }
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
}

@Composable
fun ImageView(imageBitmap: ImageBitmap?, dominantColor: Array<Int>) {
    val defaultImagePainter = painterResource(id = R.drawable.ic_launcher_foreground)
    val imagePainter = imageBitmap?.let { BitmapPainter(it) } ?: defaultImagePainter
    Image(
        painter = imagePainter,
        contentDescription = "image",
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 6.dp)
            .size(290.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                2.0.dp,
                imageBitmap?.let { Color(dominantColor[0], dominantColor[1], dominantColor[2]) }
                    ?: MaterialTheme.colorScheme.onBackground,
                RoundedCornerShape(16.dp)
            ),

        contentScale = ContentScale.Crop
    )
}

@Composable
fun ColorPaletteSection(colorList: List<Array<Int>>?) {
    if (colorList?.isNotEmpty() == true) {
        PaletteCard(colorList)
    } else {
        LoadingAnimation()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageTopAppBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = "") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                    contentDescription = "back"
                )
            }
        }
    )
}

@Composable
fun ImageContent(
    colorState: ColorViewModel.ColorState,
    showFriendSelectionPopup: Boolean,
    friendList: List<Friend>,
    viewModel: ColorViewModel,
    restClient: RestClient,
    navController: NavController
) {
    val context = LocalContext.current
    val imageBitmap = colorState.imageBitmap
    val colorList = colorState.colorList

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 10.dp, bottom = 0.dp, top = 45.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (colorList != null) {
            ImageView(imageBitmap = imageBitmap, dominantColor = colorList[0])
            ColorPaletteSection(colorList)

            ShareFab {
                viewModel.refreshFriendList(restClient)
                Log.i("ImageContent", "Friend List: ${friendList.joinToString(",")}")
                if (friendList.isNotEmpty()) {
                    viewModel.setShowFriendSelectionPopup(true)
                } else {
                    Toast.makeText(context, "You are not connected to your friends yet.", Toast.LENGTH_SHORT).show()
                }
            }

            if (showFriendSelectionPopup) {
                FriendSelectionPopup(
                    navController = navController, // Provide a NavController
                    viewModel = viewModel,
                    colorPalette = colorList,
                    friends = friendList,
                    restClient = restClient,
                    onDismiss = { viewModel.setShowFriendSelectionPopup(false) }
                )
            }
        } else {
            LoadingAnimation()
        }
    }
}

@Composable
fun ColumnScope.ShareFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .align(alignment = Alignment.End)
            .padding(end = 10.dp, bottom = 10.dp)
            .size(50.dp)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_check_24),
            contentDescription = "done"
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ImageScreen(navController: NavController, viewModel: ColorViewModel, restClient: RestClient) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val colorState: ColorViewModel.ColorState by viewModel.colorState.collectAsStateWithLifecycle(
        lifecycleOwner = lifecycleOwner
    )
    val showFriendSelectionPopup by viewModel.showFriendSelectionPopup.collectAsStateWithLifecycle(lifecycleOwner = lifecycleOwner)
    val friendList by viewModel.friendList.collectAsStateWithLifecycle(lifecycleOwner)
    BackHandler {
        navController.navigate(Screen.MainScreen.route)
        viewModel.resetColorState()
    }
    
    Column {
        ImageTopAppBar {
            navController.navigate(Screen.MainScreen.route)
            viewModel.resetColorState()
        }
        ImageContent(
            colorState = colorState,
            showFriendSelectionPopup = showFriendSelectionPopup,
            friendList = friendList,
            viewModel = viewModel,
            restClient = restClient,
            navController = navController
        )
    }
}

@Composable
fun FriendSelectionPopup(
    navController: NavController,
    viewModel: ColorViewModel,
    colorPalette: List<Array<Int>>,
    friends: List<Friend>,
    restClient: RestClient,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val selectedFriends = remember { mutableStateListOf<Friend>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Select Friends", style = MaterialTheme.typography.headlineSmall) },
        text = {
            LazyColumn {
                items(friends.size) { index ->
                    val friend = friends[index]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedFriends.contains(friend)) {
                                    selectedFriends.remove(friend)
                                } else {
                                    selectedFriends.add(friend)
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Checkbox(
                            checked = selectedFriends.contains(friend),
                            onCheckedChange = {
                                if (it) {
                                    selectedFriends.add(friend)
                                } else {
                                    selectedFriends.remove(friend)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = friend.name,
                                fontWeight = FontWeight.Medium
                            )
                            Text(text = friend.tileIds.joinToString(", "))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(enabled = selectedFriends.isNotEmpty(), onClick = {
                val selectedColors = colorPalette.map { array -> String.format("#%02X%02X%02X", array[0], array[1], array[2]) }
                selectedFriends.forEach {
                    restClient.sendColors(it.friendId, selectedColors) { _, error ->
                        if (error != null) {
                            Log.e("FriendSelectionPopup", "Failed to send colors: ${error.message}")
                        } else {
                            Log.i("FriendSelectionPopup", "Colors sent successfully")
                        }
                    }
                }
                navController.navigate(route = Screen.MainScreen.route) {
                    popUpTo(Screen.MainScreen.route) {inclusive = true}
                }
                viewModel.resetColorState()
                Toast.makeText(context, "Color palette was sent", Toast.LENGTH_LONG).show()
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PaletteCard(colorList: List<Array<Int>>) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (row in 0 until 5) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for (col in 0 until 2) {
                    val index = row * 2 + col
                    if (index < colorList.size) {
                        val colorArray = colorList[index]
                        val color = Color(colorArray[0], colorArray[1], colorArray[2])
                        // Use the new ColorBox composable
                        ColorBox(color = color, text = "(${colorArray[0]}, ${colorArray[1]}, ${colorArray[2]})")
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.ColorBox(color: Color, text: String) {
    Box(
        modifier = Modifier
            .background(color = color, shape = RoundedCornerShape(20))
            .weight(1f)
    ) {
        Text(
            text = text,
            color = if (color.luminance() > 0.5) Color.Black else Color.White,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(10.dp)
        )
    }
}

@Composable
fun LoadingAnimation() {
    val circleSize = 16.dp
    val circleColor = MaterialTheme.colorScheme.secondary
    val spaceBetween = 6.dp
    val travelDistance = 14.dp
    val distance = with(LocalDensity.current) { travelDistance.toPx() }

    val circles = listOf(
        remember { Animatable(initialValue = 0f) },
        remember { Animatable(initialValue = 0f) },
        remember { Animatable(initialValue = 0f) }
    )
    val circleValues = circles.map { it.value }

    circles.forEachIndexed { index, animatable ->
        LaunchedEffect(key1 = animatable) {
            delay(index * 100L)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1200
                        0.0f at 0 using LinearOutSlowInEasing
                        1.0f at 300 using LinearOutSlowInEasing
                        0.0f at 600 using LinearOutSlowInEasing
                        0.0f at 1200 using LinearOutSlowInEasing
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(spaceBetween),
        verticalAlignment = Alignment.CenterVertically
    ) {
        circleValues.forEachIndexed { _, value ->
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .graphicsLayer { translationY = -value * distance }
                    .background(
                        color = circleColor,
                        shape = CircleShape
                    )
            )
        }
    }
}