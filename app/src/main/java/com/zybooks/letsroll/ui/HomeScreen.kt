package com.zybooks.letsroll.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zybooks.letsroll.ui.theme.backgroundColor
import com.zybooks.letsroll.ui.theme.pastelBlue
import com.zybooks.letsroll.ui.theme.pastelRed
import com.zybooks.letsroll.ui.theme.pastelYellow

import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max


@Composable
fun Ball(
    accelerationViewModel: AccelerationViewModel = viewModel(
        factory = AccelerationViewModel.Factory
    )
){
    var ballX by remember { mutableStateOf(500f) }
    var ballY by remember { mutableStateOf(900f) }
    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }

    val accelX = -accelerationViewModel.accelValues[0]
    val accelY = accelerationViewModel.accelValues[1]

    val ballRadius = 40.dp

    val density = LocalDensity.current
    val ballRadiusPx = with(density) { ballRadius.toPx() }

    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }

    LaunchedEffect(accelX, accelY) {
        accelerationViewModel.canAccelerate = true
        while (accelerationViewModel.canAccelerate) {
            if ((abs(accelerationViewModel.oCenterX - ballX) < 10) && (abs(accelerationViewModel.oCenterY - ballY) < 10)) {
                ballX = accelerationViewModel.oCenterX
                ballY = accelerationViewModel.oCenterY
                accelerationViewModel.canAccelerate = false
                break;
            }
            velocityX += accelX * 0.5f
            velocityY += accelY * 0.5f
            velocityX *= 0.9f
            velocityY *= 0.9f

            ballX += velocityX
            ballY += velocityY

            if (ballX < ballRadiusPx) {
                ballX = ballRadiusPx
                velocityX = -velocityX
            }
            if (ballX > screenWidth - ballRadiusPx) {
                ballX = screenWidth - ballRadiusPx
                velocityX = -velocityX
            }
            if (ballY < ballRadiusPx) {
                ballY = ballRadiusPx
                velocityY = -velocityY
            }
            if (ballY > screenHeight - ballRadiusPx) {
                ballY = screenHeight - ballRadiusPx
                velocityY = -velocityY
            }


            delay(16L) // 60 FPS (update rate)
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {


        Canvas(modifier = Modifier.fillMaxSize()) {
            screenWidth = size.width
            screenHeight = size.height
            drawCircle(
                color = Color.Black,
                radius = ballRadiusPx,
                center = Offset(ballX, ballY)
            )

        }
    }

    LifecycleResumeEffect(Unit) {
        accelerationViewModel.startListening()
        onPauseOrDispose {
            accelerationViewModel.stopListening()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(
    accelerationViewModel: AccelerationViewModel = viewModel(
        factory = AccelerationViewModel.Factory
    ),
    startGame: () -> Unit,
    appPreferences: State<AppPreferences>
){
    var alertDialog by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(150.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy((-20).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LETS",
                style = TextStyle(fontSize = 100.sp, fontWeight = FontWeight.Bold ),
                lineHeight = 220.sp,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "R",
                    style = TextStyle(fontSize = 100.sp, fontWeight = FontWeight.Bold ),
                    lineHeight = 220.sp,
                )
                Canvas(modifier = Modifier
                    .size(80.dp)
                    .onGloballyPositioned { coordinates ->
                        val position = coordinates.positionInRoot()
                        accelerationViewModel.oCenterX = position.x + coordinates.size.width / 2
                        accelerationViewModel.oCenterY = position.y + coordinates.size.height / 2
                    })
                {
                    drawCircle(
                        color = Color.White,
                        radius = size.minDimension / 2
                    )
                    drawCircle(
                        color = backgroundColor,
                        radius = size.minDimension / 2 - 40,
                    )
                }
                Text(
                    text = "LL",
                    style = TextStyle(fontSize = 100.sp, fontWeight = FontWeight.Bold ),
                    lineHeight = 220.sp,
                )
            }
        }
        Column() {
            Button(
                onClick = { startGame() },
                colors = ButtonDefaults.buttonColors(containerColor = pastelBlue),
                shape = RoundedCornerShape(5.dp),
                enabled = !accelerationViewModel.canAccelerate,
                modifier = Modifier
                    .padding(5.dp)
                    .width(310.dp)
                    .height(75.dp)
            ) {
                Text("START")
            }
            Row() {
                Button(
                    onClick = { alertDialog = "S" },
                    colors = ButtonDefaults.buttonColors(containerColor = pastelRed),
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier
                        .padding(5.dp)
                        .width(150.dp)
                        .height(75.dp)
                ) {
                    Text("SETTINGS")
                }
                Button(
                    onClick = { alertDialog = "H" },
                    colors = ButtonDefaults.buttonColors(containerColor = pastelYellow),
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier
                        .padding(5.dp)
                        .width(150.dp)
                        .height(75.dp)
                ) {
                    Text("HIGH SCORE")
                }
            }
        }
        if (alertDialog == "S") {
            AlertDialog(
                title = {
                    Text(text = "Settings")
                },
                onDismissRequest = { alertDialog = "" },
                confirmButton = {
                    Button(onClick = { alertDialog = "" }) {
                        Text(text = "X")
                    }
                }
            )

        } else if (alertDialog == "H") {
//            AlertDialog(
//                title = {
//                    Text(text = "High Score")
//                },
//                onDismissRequest = { alertDialog = "" },
//                confirmButton = {
//                    Button(onClick = { alertDialog = "" }) {
//                        Text(text = "X")
//                    }
//                },
//                text = { Text(appPreferences.value.highScore.toString()) }
//            )
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp
            val screenHeight = configuration.screenHeightDp.dp
            BasicAlertDialog(
                onDismissRequest = {},
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center) // Centers the content in the Box
                    ) {
                        Box(
                            modifier = Modifier
                                .width(screenWidth / 3 * 2)
                                .height(screenHeight / 3)
                                .background(Color.White, shape = RoundedCornerShape(10.dp)) // Optional: Add background color
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "GAME OVER",
                                    fontSize = 35.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}


@Composable
fun HomeScreen(
    viewModel: AccelerationViewModel = viewModel(
        factory = AccelerationViewModel.Factory
    ),
    startGame: () -> (Unit),
    appPreferences: State<AppPreferences>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Screen(viewModel, startGame, appPreferences)
        Ball(viewModel)
    }
}