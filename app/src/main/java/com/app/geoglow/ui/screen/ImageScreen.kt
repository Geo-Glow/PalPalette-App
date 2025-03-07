package com.app.geoglow.ui.screen

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.app.geoglow.R
import com.app.geoglow.SendColorsResult
import com.app.geoglow.data.model.Friend
import com.app.geoglow.network.client.RestClient
import com.app.geoglow.ui.composable.DraggablePalette
import com.app.geoglow.ui.composable.LoadingAnimation
import com.app.geoglow.ui.navigation.Screen
import com.app.geoglow.utils.storage.DataStoreManager
import com.app.geoglow.viewmodel.ColorViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Composable
fun ImageView(
    imageBitmap: ImageBitmap?,
    dominantColor: Array<Int>
) {
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
                if (imageBitmap != null) Color(dominantColor[0], dominantColor[1], dominantColor[2])
                else MaterialTheme.colorScheme.onBackground,
                RoundedCornerShape(16.dp)
            ),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ColorPaletteSection(viewModel: ColorViewModel) {
    val colorState by viewModel.colorState.collectAsState()
    colorState.colorList?.let { colorList ->
        if (colorList.isNotEmpty()) {
            DraggablePalette(viewModel = viewModel)
        } else {
            LoadingAnimation()
        }
    } ?: LoadingAnimation()
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
    fromFriendId: String,
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
            ColorPaletteSection(viewModel = viewModel)

            if (showFriendSelectionPopup) {
                FriendSelectionPopup(
                    navController = navController,
                    viewModel = viewModel,
                    colorPalette = colorList,
                    friends = friendList,
                    restClient = restClient,
                    fromFriendId = fromFriendId,
                    onDismiss = { viewModel.setShowFriendSelectionPopup(false) }
                )
            }
        } else {
            LoadingAnimation()
        }
    }
}

@Composable
fun ShareFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
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
fun ImageScreen(
    navController: NavController,
    viewModel: ColorViewModel,
    restClient: RestClient,
) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val colorState: ColorViewModel.ColorState by viewModel.colorState.collectAsStateWithLifecycle(
        lifecycleOwner = lifecycleOwner
    )
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    var fromFriendId by remember { mutableStateOf("") }

    LaunchedEffect(colorState.errorMessage) {
        colorState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            navController.navigateUp()
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(Unit) {
        fromFriendId = dataStoreManager.friendID.first()
    }

    val showFriendSelectionPopup by viewModel.showFriendSelectionPopup.collectAsStateWithLifecycle(
        lifecycleOwner
    )
    val friendList by viewModel.friendList.collectAsStateWithLifecycle(lifecycleOwner)

    BackHandler {
        navController.navigate(Screen.MainScreen.route)
        viewModel.resetColorState()
    }

    Scaffold(
        topBar = {
            ImageTopAppBar {
                navController.navigate(Screen.MainScreen.route)
                viewModel.resetColorState()
            }
        },
        floatingActionButton = {
            ShareFab {
                viewModel.refreshFriendList(restClient)
                if (friendList.isNotEmpty()) {
                    viewModel.setShowFriendSelectionPopup(true)
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_friends_not_connected),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ImageContent(
                colorState = colorState,
                showFriendSelectionPopup = showFriendSelectionPopup,
                friendList = friendList,
                viewModel = viewModel,
                restClient = restClient,
                fromFriendId = fromFriendId,
                navController = navController
            )
        }
    }
}

@Composable
fun FriendSelectionPopup(
    navController: NavController,
    viewModel: ColorViewModel,
    colorPalette: List<Array<Int>>,
    friends: List<Friend>,
    restClient: RestClient,
    fromFriendId: String,
    onDismiss: () -> Unit
) {
    val DEV_MODE = true
    val context = LocalContext.current
    val dataStoreManager = DataStoreManager(context)
    val selectedFriends = remember { mutableStateListOf<Friend>() }

    fun Color.toLuminance(): Float {
        return 0.299f * red + 0.587f * green + 0.114f * blue
    }

    @Composable
    fun getTextColorForBackground(backgroundColor: Color): Color {
        return if (backgroundColor.toLuminance() > 0.5f) Color.Black else Color.White
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.friends_select),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            LazyColumn {
                items(friends.size) { index ->
                    val friend = friends[index]
                    val ownFriendId = runBlocking {
                        dataStoreManager.friendID.first()
                    }
                    if (friend.friendId != ownFriendId || DEV_MODE) {
                        val backGroundColor = Color(
                            red = friend.color[0],
                            green = friend.color[1],
                            blue = friend.color[2]
                        )
                        val textColor = getTextColorForBackground(backGroundColor)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(backGroundColor)
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
                                },
                                colors = CheckboxDefaults.colors(checkmarkColor = textColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(friend.name, fontWeight = FontWeight.Medium, color = textColor)
                                //Text(friend.tileIds.joinToString(", "), color = textColor)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(enabled = selectedFriends.isNotEmpty(), onClick = {
                val selectedColors = colorPalette.map { array ->
                    String.format(
                        "#%02X%02X%02X",
                        array[0],
                        array[1],
                        array[2]
                    )
                }
                restClient.sendColors(
                    fromFriendId,
                    selectedFriends.map { it.friendId },
                    selectedColors,
                    viewModel.colorState.value.imageMetadataJson
                ) { result, error ->
                    val message = when (result) {
                        SendColorsResult.SUCCESS -> "Colors sent successfully"
                        SendColorsResult.ACCEPTED -> "Friend currently offline, the Message will be processed later"
                        SendColorsResult.FRIEND_NOT_FOUND -> "Friend not found: $fromFriendId"
                        SendColorsResult.SERVER_ERROR -> "Server error, please try again"
                        SendColorsResult.UNKNOWN_ERROR -> "Unknown error, please try again"
                        SendColorsResult.PARTIAL_SUCCESS -> "Partial success, some friends were not found"
                        SendColorsResult.BAD_REQUEST -> "Invalid request data"
                    }

                    if (error != null) {
                        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }

                    navController.navigate(Screen.MainScreen.route) {
                        popUpTo(Screen.MainScreen.route) { inclusive = true }
                    }
                    viewModel.resetColorState()
                }
            }) {
                Text(text = stringResource(R.string.button_confirm))
            }
        },
        dismissButton = { Button(onDismiss) { Text(stringResource(R.string.button_cancle)) } }
    )
}