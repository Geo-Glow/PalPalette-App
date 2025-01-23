package com.example.geoglow.ui.composable

import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.geoglow.R
import com.example.geoglow.viewmodel.ColorViewModel
import kotlinx.coroutines.delay
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggablePalette(viewModel: ColorViewModel, modifier: Modifier = Modifier) {
    val colorState by viewModel.colorState.collectAsState()
    val colorList = remember {
        mutableStateListOf<Array<Int>>().apply {
            addAll(
                colorState.colorList ?: listOf()
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        var draggedBoxIndex by remember { mutableIntStateOf(-1) }
        val isDragging = remember { mutableStateOf(false) }

        colorList.chunked(2).forEachIndexed { rowIndex, colorPair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp), // Reduced padding between rows
                horizontalArrangement = Arrangement.spacedBy(5.dp) // Reduced space between items in the same row
            ) {
                colorPair.forEachIndexed { colIndex, colorArray ->
                    val globalIndex = rowIndex * 2 + colIndex
                    val color = Color(colorArray[0], colorArray[1], colorArray[2]).copy(alpha = 1f)

                    val dragSourceModifier = Modifier.dragAndDropSource {
                        detectTapGestures(
                            onLongPress = {
                                isDragging.value = true
                                draggedBoxIndex = globalIndex

                                startTransfer(
                                    DragAndDropTransferData(
                                        clipData = ClipData.newPlainText(
                                            "index",
                                            globalIndex.toString()
                                        )
                                    )
                                )
                            }
                        )
                    }

                    val dropTargetModifier = Modifier.dragAndDropTarget(
                        shouldStartDragAndDrop = { event ->
                            event.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                        },
                        target = remember {
                            object : DragAndDropTarget {
                                override fun onDrop(event: DragAndDropEvent): Boolean {
                                    val clipDataItem = event.toAndroidDragEvent().clipData
                                    val draggedIndexText =
                                        clipDataItem?.getItemAt(0)?.text?.toString()
                                    val draggedIndex = draggedIndexText?.toIntOrNull()

                                    if (draggedIndex != null && draggedIndex != globalIndex) {
                                        colorList.swap(draggedIndex, globalIndex)
                                        viewModel.updateColorList(colorList.toList())
                                    }

                                    isDragging.value = false
                                    draggedBoxIndex = -1
                                    return true
                                }
                            }
                        }
                    )

                    ColorBox(
                        color,
                        "",
                        index = colorList.indexOf(colorArray) + 1, // We need to add 1 since Arrays start at 0
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .height(50.dp)
                            .then(dragSourceModifier)
                            .then(dropTargetModifier)
                            .background(
                                color = color.copy(alpha = if (isDragging.value && globalIndex == draggedBoxIndex) 0.5f else 1f),
                                shape = RoundedCornerShape(20)
                            )
                    )
                }
            }
        }
    }
}

private fun MutableList<Array<Int>>.swap(index1: Int, index2: Int) {
    val temp = this[index1]
    this[index1] = this[index2]
    this[index2] = temp
}

@Composable
fun RowScope.ColorBox(color: Color, text: String, index: Int, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .weight(1f)
            .height(50.dp)
            .background(color, shape = RoundedCornerShape(20))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (color.luminance() > 0.5) Color.DarkGray else Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                color = if (color.luminance() > 0.5) Color.White else Color.Black,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(color, shape= RoundedCornerShape(20))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            val iconColor = if (color.luminance() > 0.5) Color.Black else Color.White

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Drag handle",
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


@Composable
fun LoadingAnimation() {
    val circleSize = 16.dp
    val circleColor = MaterialTheme.colorScheme.secondary
    val spaceBetween = 6.dp
    val travelDistance = 14.dp
    val distance = with(androidx.compose.ui.platform.LocalDensity.current) { travelDistance.toPx() }

    val circles = listOf(
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) }
    )
    val circleValues = circles.map { it.value }

    circles.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
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
        horizontalArrangement = Arrangement.spacedBy(spaceBetween),
        verticalAlignment = Alignment.CenterVertically
    ) {
        circleValues.forEach { value ->
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

@Composable
fun DatePickerDialog(
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    calendar.timeInMillis = initialDate

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            onDateSelected(calendar.timeInMillis)
            onDismissRequest()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

@Composable
fun CustomDatePickerDialog(
    initialStartDate: Long,
    initialEndDate: Long,
    onDismissRequest: () -> Unit,
    onDatesSelected: (Long, Long) -> Unit
) {
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        DatePickerDialog(
            initialDate = startDate,
            onDateSelected = { date -> startDate = date },
            onDismissRequest = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            initialDate = endDate,
            onDateSelected = { date -> endDate = date },
            onDismissRequest = { showEndDatePicker = false }
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Select Start Date:")
        Button(onClick = { showStartDatePicker = true }) {
            Text(text = "Pick Start Date")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Select End Date:")
        Button(onClick = { showEndDatePicker = true }) {
            Text(text = "Pick End Date")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    onDismissRequest()
                }) {
                Text(text = stringResource(R.string.button_cancle))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    onDatesSelected(startDate, endDate)
                    onDismissRequest()
                }) {
                Text(text = "Apply")
            }
        }
    }
}