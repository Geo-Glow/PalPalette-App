package com.example.geoglow.ui.composable

import android.app.DatePickerDialog
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.node.CanFocusChecker.start
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.geoglow.R
import com.example.geoglow.ui.screen.ColorBox
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun PaletteCard(colorList: List<Array<Int>>) {
    val rowSpacing = 8.dp
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            colorList.chunked(2).forEach { colorPair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    colorPair.forEach { colorArray ->
                        val color = Color(colorArray[0], colorArray[1], colorArray[2])
                        ColorBox(color, "(${colorArray[0]}, ${colorArray[1]}, ${colorArray[2]})")
                    }
                    if (colorPair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(rowSpacing))
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
            .height(50.dp) // Height of each ColorBox
    ) {
        Text(
            text = "", // or text,
            color = if (color.luminance() > 0.5) Color.Black else Color.White,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
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
        Button(onClick = { showStartDatePicker = true}) {
            Text(text = "Pick Start Date")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Select End Date:")
        Button(onClick = { showEndDatePicker = true}) {
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