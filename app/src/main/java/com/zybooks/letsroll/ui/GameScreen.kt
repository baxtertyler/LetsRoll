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

fun generateVerticalLine(
    screenHeight: Float,
    startPoint: Offset
):List<Offset> {
    val points = mutableListOf<Offset>()
    points.add(startPoint)
    points.add(Offset(startPoint.x, startPoint.y + screenHeight / 2))
    return points
}

fun generateHorizontalLine(
    screenWidth: Float,
    startPoint: Offset,
    toRight: Boolean,
):List<Offset> {
    val points = mutableListOf<Offset>()
    points.add(startPoint)
    if (toRight) {
        points.add(Offset(startPoint.x + screenWidth, startPoint.y))
    } else {
        points.add(Offset(startPoint.x - screenWidth, startPoint.y))
    }
    return points
}

fun generateAngleLine(
    screenHeight: Float,
    screenWidth: Float,
    startPoint: Offset,
    openLeft: Boolean
):List<Offset> {
    val points = mutableListOf<Offset>()
    points.add(startPoint)
    points.add(Offset(startPoint.x, startPoint.y + screenHeight / 8))
    if (openLeft) {
        points.add(Offset(startPoint.x + screenWidth / 2, startPoint.y + screenHeight / 4 + screenHeight / 8))
    } else {
        points.add(Offset(startPoint.x - screenWidth / 2, startPoint.y + screenHeight / 4 + screenHeight / 8))
    }
    points.add(Offset(startPoint.x, startPoint.y + screenHeight / 4 + screenHeight / 4 + screenHeight / 8))
    points.add(Offset(startPoint.x, startPoint.y + screenHeight / 4 + screenHeight / 4 + screenHeight / 4 + screenHeight / 8))
    return points
}

//fun generateCosWave(
//    startingPoint: Offset,
//    screenHeight: Float,
//    screenWidth: Float
//): List<Offset> {
//    val points = mutableListOf<Offset>()
//    val amplitude = ((screenWidth / 4).toInt()..(screenWidth / 1.5).toInt()).random() // width
//    val frequency = 2 * 3.141592653589 / screenWidth
//    val phaseShift = 1
//
//    for (y in startingPoint.y.toInt()..(screenHeight / phaseShift + startingPoint.y.toInt()).toInt() step 5) {
//        val x = amplitude * kotlin.math.sin(frequency * y + phaseShift) + (screenWidth / 2)
//        points.add(Offset(x.toFloat(), y.toFloat()))
//    }
//    return points
//}


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

    var pathPoints by remember { mutableStateOf(listOf(Offset(0f, 0f))) }

    if (loading && screenWidth > 0F && screenHeight > 0F) {
        loading = false
        ballX = screenWidth / 2
        ballY = screenHeight / 2
    }

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

            Log.d("hi", pathPoints.size.toString() + " " + -ballY + " " + pathPoints.last().y)

            if (pathPoints.isNotEmpty() && -ballY > pathPoints.last().y - screenHeight) {
                val lineGenerators = listOf(
                    { generateVerticalLine(screenHeight, pathPoints.last()) },
                    { generateAngleLine(screenHeight, screenWidth, pathPoints.last(), (0..1).random() == 1) },
                    { generateHorizontalLine(screenWidth, pathPoints.last(), (0..1).random() == 1) }
                )
                pathPoints += lineGenerators.random().invoke()
            }

            if (pathPoints.size > 20) {
                pathPoints = pathPoints.drop(1)
            }

            delay(16L) // 60 FPS (update rate)
        }
    }

    LaunchedEffect(Unit) {
        pathPoints = generateVerticalLine(screenHeight, Offset(0F, 0F))
        pathPoints += generateAngleLine(screenHeight, screenWidth, pathPoints.last(), false)
        pathPoints += generateAngleLine(screenHeight, screenWidth, pathPoints.last(), true)
        pathPoints += generateHorizontalLine(screenWidth, pathPoints.last(),(0..1).random() == 1)
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
                    start = Offset(pathPoints[i].x + ballX, pathPoints[i].y + ballY),
                    end = Offset(pathPoints[i+1].x + ballX, pathPoints[i+1].y + ballY),
                    strokeWidth = 250f
                )
                drawCircle(
                    color = Color.Black,
                    center = Offset(pathPoints[i].x + ballX, pathPoints[i].y + ballY),
                    radius = 125F
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
    }

    LifecycleResumeEffect(Unit) {
        viewModel.startListening()

        onPauseOrDispose {
            viewModel.stopListening()
        }
    }
}
