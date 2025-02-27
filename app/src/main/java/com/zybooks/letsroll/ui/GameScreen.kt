package com.zybooks.letsroll.ui

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

fun generateSinePath(
    screenHeight: Float,
    screenWidth: Float,
    startY: Float = 0f,
): List<Offset> {
    val points = mutableListOf<Offset>()
    val amplitude = ((screenWidth / 4).toInt()..(screenWidth / 2).toInt()).random() // width
    val frequency = (1..3).random() * 0.002f
    val phaseShift = (0..360).random().toFloat()

    for (y in startY.toInt()..(screenHeight + startY).toInt() step 1) {
        val x = amplitude * kotlin.math.cos(frequency * y + phaseShift) + (screenWidth / 2)
        points.add(Offset(x, y.toFloat()))
    }
    return points
}

fun generateCosWave(
    startingPoint: Offset,
    screenHeight: Float,
    screenWidth: Float
): List<Offset> {
    val points = mutableListOf<Offset>()
    val amplitude = ((screenWidth / 4).toInt()..(screenWidth / 2).toInt()).random() // width
    val frequency = 2 * 3.14 / screenWidth
    val phaseShift = 1

    for (y in startingPoint.y.toInt()..(screenHeight / phaseShift + startingPoint.y.toInt()).toInt() step 15) {
        val x = amplitude * kotlin.math.sin(frequency * y + phaseShift) + (screenWidth / 2)
        points.add(Offset(x.toFloat(), y.toFloat()))
    }
    return points
}


@Composable
fun GameScreen(
    viewModel: AccelerationViewModel = viewModel(
        factory = AccelerationViewModel.Factory
    )
) {
    var loading by remember { mutableStateOf(true)}
    var ballX by remember { mutableStateOf(0F) }
    var ballY by remember { mutableStateOf(0f) }
    var shadowOffsetX by remember { mutableStateOf(0F) }
    var shadowOffsetY by remember { mutableStateOf(0f) }

    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }

    val accelX = viewModel.accelValues[0]
    val accelY = -viewModel.accelValues[1]

    val ballRadius = 30.dp

    val density = LocalDensity.current
    val ballRadiusPx = with(density) { ballRadius.toPx() }

    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }

    var currentYLim by remember { mutableStateOf(0f)}

    var pathPoints by remember { mutableStateOf(generateSinePath(screenHeight, screenWidth)) }

    if (loading && screenWidth > 0F && screenHeight > 0F) {
        loading = false
        ballX = screenWidth / 2
        ballY = screenHeight / 2
    }

    var statusMessage by remember { mutableStateOf("") }

    LaunchedEffect(accelX, accelY) { //  run in bg!
        while (true) {
            velocityX += accelX * 0.5f
            velocityY += accelY * 0.5f
            velocityX *= 0.9f
            velocityY *= 0.9f
            ballX += velocityX
            ballY += velocityY
            shadowOffsetX = -velocityX
            shadowOffsetY = -velocityY

            val s = pathPoints.size
            Log.d("GameScreen", "ballY: $s")

            if (ballY < currentYLim && pathPoints.isNotEmpty()) {
                currentYLim -= screenHeight
                val path = generateCosWave(pathPoints[pathPoints.size - 1], screenHeight, screenWidth)
                pathPoints = pathPoints + path
            }

            if (pathPoints.size > 6000) {
                pathPoints = pathPoints.drop(50)
            }
            delay(16L) // 60 FPS (update rate)
        }
    }

    LaunchedEffect(Unit) {
        pathPoints = generateCosWave(Offset(screenHeight / 2, screenWidth / 2), screenHeight, screenWidth)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.Black,
                center = Offset(ballX, ballY),
                radius = 300F
            )

        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0 until pathPoints.size - 1) {
                drawLine(
                    color = Color.Black,
                    start = Offset(pathPoints[i].x + ballX - 500, pathPoints[i].y + ballY),
                    end = Offset(pathPoints[i+1].x + ballX - 500, pathPoints[i+1].y + ballY),
                    strokeWidth = 250f
                )
            }
        }

        // ball
        Canvas(modifier = Modifier.fillMaxSize()) {
            screenWidth = size.width
            screenHeight = size.height
            drawCircle(
                color = Color.Blue,
                radius = ballRadiusPx,
                center = Offset(size.width / 2, size.height / 2)
            )
        }
        Column {
            Text(statusMessage, color = Color.Green)
        }
    }

    LifecycleResumeEffect(Unit) {
        viewModel.startListening()

        onPauseOrDispose {
            viewModel.stopListening()
        }
    }
}
