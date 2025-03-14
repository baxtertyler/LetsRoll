package tbax.gamedev.letsroll.letsroll.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt
import tbax.gamedev.letsroll.letsroll.ui.theme.backgroundColor
import tbax.gamedev.letsroll.letsroll.ui.theme.pastelBlue
import tbax.gamedev.letsroll.letsroll.ui.theme.pastelRed
import kotlin.math.atan
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random


fun generateVerticalLine(
    screenHeight: Float,
    startPoint: Offset
):List<Offset> {
    val points = mutableListOf<Offset>()
    points.add(startPoint)
    points.add(Offset(startPoint.x, startPoint.y + screenHeight / 4))
    return points
}

fun generateBackAngleLine(
    screenHeight: Float,
    startPoint: Offset,
    openLeft: Boolean
):List<Offset> {
    val points = mutableListOf<Offset>()
    points.add(startPoint)
    points.add(Offset(startPoint.x, startPoint.y + screenHeight / 8))
    if (openLeft) {
        points.add(Offset(startPoint.x + screenHeight / 4, startPoint.y + screenHeight / 4 + screenHeight / 8))
    } else {
        points.add(Offset(startPoint.x - screenHeight / 4, startPoint.y + screenHeight / 4 + screenHeight / 8))
    }
    points.add(Offset(startPoint.x, startPoint.y + screenHeight / 4 + screenHeight / 4 + screenHeight / 8))
    points.add(Offset(startPoint.x, startPoint.y + screenHeight / 4 + screenHeight / 4 + screenHeight / 4 + screenHeight / 8))
    return points
}

fun generateAngleLine(
    screenHeight: Float,
    startPoint: Offset,
): List<Offset> {
    val points = mutableListOf<Offset>()
    points.add(startPoint)
    val y = listOf(screenHeight / 2, screenHeight / 4).random()
    val x = Random.nextDouble(-y.toDouble(), y.toDouble()).toFloat()
    points.add(Offset((startPoint.x + x), (startPoint.y + y)));
    return points
}

fun generateZigZag(
    screenHeight: Float,
    startPoint: Offset,
):List<Offset> {
    val add = screenHeight / 8
    val points = mutableListOf<Offset>()
    points.add(startPoint)
    points.add(Offset(startPoint.x + add, startPoint.y + add))
    points.add(Offset(startPoint.x, startPoint.y + 2 * add))
    points.add(Offset(startPoint.x + add, startPoint.y + 3 * add))
    points.add(Offset(startPoint.x, startPoint.y + 4 * add))
    points.add(Offset(startPoint.x + add, startPoint.y + 5 * add))
    return points
}

fun check(ballX: Float, ballY: Float, p1: Offset, p2: Offset, radius: Float): Boolean {
    return checkPath(ballX, ballY, p1, p2)
}

fun checkPath(ballX: Float, ballY: Float, p1: Offset, p2: Offset): Boolean {
    if (p1.x == p2.x) { // vertical line
        return abs(ballX - p1.x) < 125
    }
    if (abs(p1.x - p2.x) == abs(p1.y - p2.y)) { // 45* angle line
        val m = (p2.y - p1.y) / (p2.x - p1.x)
        val b = p1.y - m * p1.x
        return abs(m * ballX + b - ballY) < (125 * sqrt(2F))
    }
    if (p1.x > p2.x) { // angle left
        val m = (p2.y - p1.y) / (p2.x - p1.x)
        val b = p1.y - m * p1.x
        val h = p2.y - p1.y
        val w = p1.x - p2.x
        val maxX = 125.0 / sin(atan(h / w))
        val currX = ballX - ((ballY - b) / m)
        return ((abs(currX) - maxX) < 0)
    }
    if (p1.x < p2.x) { // angle right
        val m = (p2.y - p1.y) / (p2.x - p1.x)
        val b = p1.y - m * p1.x
        val h = p2.y - p1.y
        val w = p2.x - p1.x
        val maxX = 125.0 / sin(atan(h/w))
        val currX = ballX - ((ballY - b) / m)
        return (abs(currX) - maxX) < 0
    }
    return false
}

fun checkCircle(ballX: Float, ballY: Float, p: Offset, r: Float): Boolean {
    return sqrt((p.x - ballX) * (p.x - ballX) + (p.y - ballY) * (p.y - ballY)) < r
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun GameScreen(
    accelerationViewModel: AccelerationViewModel = viewModel(
        factory = AccelerationViewModel.Factory
    ),
    complete: () -> (Unit),
    store: AppStorage,
    appPreferences: State<AppPreferences>,
    settings: SettingsViewModel
) {
    var loading by remember { mutableStateOf(true)}
    var ballX by remember { mutableStateOf(0F) }
    var ballY by remember { mutableStateOf(0f) }

    var pseudoX by remember { mutableStateOf(0F) }
    var pseudoY by remember { mutableStateOf(0F) }

    var velocityX by remember { mutableStateOf(0f) }
    var velocityY by remember { mutableStateOf(0f) }

    val accelX = accelerationViewModel.accelValues[0]
    val accelY = -accelerationViewModel.accelValues[1]

    val ballRadius = 30.dp

    val density = LocalDensity.current
    val ballRadiusPx = with(density) { ballRadius.toPx() }

    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }

    var pathPoints by remember { mutableStateOf(listOf(Offset(0f, 0f))) }

    var ballOnPath by remember { mutableStateOf(true) }
    var gameIsOver by remember { mutableStateOf(false) }

    fun endGame(goHome: Boolean) {
        // Reset ball position
        ballX = screenWidth / 2
        ballY = screenHeight / 2

        // Reset velocity and pseudo velocity
        velocityX = 0f
        velocityY = 0f
        pseudoX = 0f
        pseudoY = 0f

        // Reset path points
        pathPoints = generateVerticalLine(screenHeight, Offset(0F, 0F))
        pathPoints += generateBackAngleLine(screenHeight, pathPoints.last(), (0..1).random() == 1)

        // Restart listening for accelerometer input
        accelerationViewModel.canAccelerate = true

        if (goHome) {
            complete()
            ballOnPath = true
            gameIsOver = false
        }

        ballOnPath = true
        gameIsOver = false
    }


    if (loading && screenWidth > 0F && screenHeight > 0F) {
        loading = false
        ballX = screenWidth / 2
        ballY = screenHeight / 2
    }

    LaunchedEffect(accelX, accelY) { //  run in bg!
        while (accelerationViewModel.canAccelerate) {

            velocityX += accelX * 0.5f
            velocityY += accelY * 0.5f
            velocityX *= 0.9f
            velocityY *= 0.9f
            ballX += velocityX
            ballY += velocityY
            pseudoX += -velocityX
            pseudoY += -velocityY

            if (pathPoints.isNotEmpty() && -ballY > pathPoints.last().y - screenHeight) {
                val nextPath = listOf(1, 2, 3, 4, 5, 6).random()
                if (nextPath == 6) {
                    pathPoints += generateBackAngleLine(screenHeight, pathPoints.last(), (0..1).random() == 1)
                } else if (nextPath == 5) {
                    pathPoints += generateZigZag(screenHeight, pathPoints.last())
                } else {
                    pathPoints += generateAngleLine(screenHeight / 2, pathPoints.last())
                }
            }

            if (pathPoints.size > 50) {
                pathPoints = pathPoints.drop(1)
            }

            delay(16L) // 60 FPS (update rate)
        }
    }

    LaunchedEffect(Unit) {
        pathPoints = generateVerticalLine(screenHeight, Offset(0F, 0F))
        pathPoints += generateBackAngleLine(screenHeight, pathPoints.last(), (0..1).random() == 1)
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
                if (pseudoY < 275 && pseudoY > -400) {
                    if (checkCircle(pseudoX, pseudoY, Offset(0F, 0F), 300F)) {
                        ballOnPath = true
                    } else {
                        ballOnPath = false
                        gameIsOver = true
                        accelerationViewModel.canAccelerate = false
                    }
                }
                else if (pseudoY > 300F) {
                    if (i < pathPoints.size - 1 && pathPoints[i].y < pseudoY && pathPoints[i+1].y > pseudoY) {
                        if (check(pseudoX, pseudoY, pathPoints[i], pathPoints[i+1], ballRadiusPx)) {
                            ballOnPath = true
                        } else {
                            ballOnPath = false
                            gameIsOver = true
                            accelerationViewModel.canAccelerate = false
                        }
                    }
                }
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

        // score
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(backgroundColor, shape = RoundedCornerShape(16.dp))
                    .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(16.dp))
                    .padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 10.dp)
            ) {
                Text(
                    text = max(pseudoY / 100, 0F).toInt().toString() + "m",
                )
            }
        }

        // ball
        Canvas(modifier = Modifier.fillMaxSize()) {
            screenWidth = size.width
            screenHeight = size.height
            drawCircle(
                color = colorMap[settings.ballColor.roundToInt()] ?: Color.Blue,
                radius = ballRadiusPx,
                center = Offset(size.width / 2, size.height / 2)
            )
        }

        if (gameIsOver) {
            LaunchedEffect(Unit) {
                store.saveHighScore(max(pseudoY / 100, 0F).toInt())
            }
            val screenWidthDp: Dp = with(LocalDensity.current) {
                screenWidth.toDp()
            }
            val screenHeightDp: Dp = with(LocalDensity.current) {
                screenHeight.toDp()
            }

            BasicAlertDialog(
                onDismissRequest = {
                    endGame(false)
                },
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(screenWidthDp / 3 * 2)
                                .height(screenHeightDp / 3)
                                .background(Color.White, shape = RoundedCornerShape(10.dp))
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
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = max(pseudoY / 100, 0F).toInt().toString() + "m",
                                    fontSize = 70.sp,
                                    fontWeight = FontWeight.Bold,
                                )

                                Text(
                                    text = "BEST: ${appPreferences.value.highScore}m",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { endGame(true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = pastelRed),
                                        shape = RoundedCornerShape(5.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(40.dp)
                                    ) {
                                        Text(text = "HOME", fontSize = 15.sp)
                                    }
                                    Button(
                                        onClick = { endGame(false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = pastelBlue),
                                        shape = RoundedCornerShape(5.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(40.dp)
                                    ) {
                                        Text(text = "REPLAY", fontSize = 15.sp)
                                    }
                                }
                            }
                        }
                    }
                }
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
