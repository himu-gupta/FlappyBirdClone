package com.example.flappybirdclone.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flappybirdclone.theme.FlappyBirdCloneTheme
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield

@Composable
fun MainScreen(
  modifier: Modifier = Modifier,
  highScoreStore: HighScoreStore = rememberHighScoreStore(),
) {
  val highScoreTracker = remember(highScoreStore) { HighScoreTracker(highScoreStore) }
  var world by remember { mutableStateOf(freshWorld()) }
  var highScore by remember(highScoreTracker) { mutableIntStateOf(highScoreTracker.highScore) }
  var viewportAspectRatio by remember { mutableStateOf(1f) }

  LaunchedEffect(world.phase) {
    if (world.phase != GamePhase.Playing) return@LaunchedEffect

    var lastFrameNanos = 0L
    while (isActive) {
      withFrameNanos { frameNanos ->
        if (lastFrameNanos != 0L) {
          val deltaSeconds = (frameNanos - lastFrameNanos) / 1_000_000_000f
          world = world.tick(deltaSeconds, viewportAspectRatio)
        }
        lastFrameNanos = frameNanos
      }
      yield()
    }
  }

  LaunchedEffect(world.score) {
    highScore = highScoreTracker.record(world.score)
  }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .onSizeChanged { size ->
          if (size.height > 0) viewportAspectRatio = size.width.toFloat() / size.height
        }
        .background(Color(0xFF8AD7F8))
        .pointerInput(Unit) { detectTapGestures { world = world.onTap() } }
        .semantics { contentDescription = "Flappy Bird game field" }
        .testTag("flappy_game"),
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawWorld(world)
    }

    GameHud(
      score = world.score,
      highScore = highScore,
      phase = world.phase,
      modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 20.dp, vertical = 16.dp),
    )
  }
}

@Composable
private fun GameHud(score: Int, highScore: Int, phase: GamePhase, modifier: Modifier = Modifier) {
  Box(modifier = modifier) {
    Column(
      modifier = Modifier.align(Alignment.TopCenter),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = "Flappy Bird",
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Black,
      )
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ScoreBadge(label = "SCORE", value = score)
        ScoreBadge(label = "BEST", value = highScore)
      }
    }

    if (phase != GamePhase.Playing) {
      Surface(
        modifier = Modifier.align(Alignment.Center),
        color = Color(0xDD15313F),
        shape = RoundedCornerShape(8.dp),
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = if (phase == GamePhase.GameOver) "Game Over" else "Ready",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
          )
          Text(
            text = if (phase == GamePhase.GameOver) "Tap to retry" else "Tap to flap",
            color = Color(0xFFEAF9FF),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }
  }
}

@Composable
private fun ScoreBadge(label: String, value: Int) {
  Surface(color = Color(0xCC15313F), shape = RoundedCornerShape(8.dp)) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
      Text(
        text = label,
        color = Color(0xFFBFEFFF),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = value.toString(),
        color = Color.White,
        fontSize = 21.sp,
        fontWeight = FontWeight.Black,
      )
    }
  }
}

private fun DrawScope.drawWorld(world: GameWorld) {
  drawRect(Brush.verticalGradient(listOf(Color(0xFF7DD4F6), Color(0xFFC6F0FF))), size = size)
  drawClouds()

  val groundTop = toPxY(groundY())
  world.pipes.forEach { pipe -> drawPipe(pipe) }
  drawGround(groundTop)
  drawBird(world)
}

private fun DrawScope.drawClouds() {
  val cloudColor = Color.White.copy(alpha = 0.78f)
  fun cloud(x: Float, y: Float, scale: Float) {
    drawOval(cloudColor, topLeft = Offset(x, y), size = Size(98f * scale, 42f * scale))
    drawOval(cloudColor, topLeft = Offset(x + 38f * scale, y - 20f * scale), size = Size(72f * scale, 58f * scale))
    drawOval(cloudColor, topLeft = Offset(x + 84f * scale, y + 4f * scale), size = Size(86f * scale, 38f * scale))
  }
  cloud(size.width * 0.10f, size.height * 0.14f, 0.9f)
  cloud(size.width * 0.58f, size.height * 0.22f, 0.72f)
  cloud(size.width * 0.30f, size.height * 0.34f, 0.55f)
}

private fun DrawScope.drawPipe(pipe: Pipe) {
  val pipeX = toPxX(pipe.x)
  val pipeW = toPxX(pipeWidth())
  val gapTop = toPxY(pipe.gapCenter - pipeGap() / 2f)
  val gapBottom = toPxY(pipe.gapCenter + pipeGap() / 2f)
  val groundTop = toPxY(groundY())
  val capHeight = 24.dp.toPx()
  val capOverhang = 10.dp.toPx()
  val radius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
  val pipeBrush = Brush.horizontalGradient(listOf(Color(0xFF38A74C), Color(0xFF65D26E), Color(0xFF207A39)))

  drawRoundRect(pipeBrush, Offset(pipeX, 0f), Size(pipeW, gapTop), radius)
  drawRoundRect(
    brush = pipeBrush,
    topLeft = Offset(pipeX - capOverhang, gapTop - capHeight),
    size = Size(pipeW + capOverhang * 2f, capHeight),
    cornerRadius = radius,
  )

  drawRoundRect(pipeBrush, Offset(pipeX, gapBottom), Size(pipeW, groundTop - gapBottom), radius)
  drawRoundRect(
    brush = pipeBrush,
    topLeft = Offset(pipeX - capOverhang, gapBottom),
    size = Size(pipeW + capOverhang * 2f, capHeight),
    cornerRadius = radius,
  )

  drawLine(Color(0xAAE4FFD6), Offset(pipeX + pipeW * 0.25f, 0f), Offset(pipeX + pipeW * 0.25f, gapTop), 3.dp.toPx())
  drawLine(Color(0xAAE4FFD6), Offset(pipeX + pipeW * 0.25f, gapBottom), Offset(pipeX + pipeW * 0.25f, groundTop), 3.dp.toPx())
}

private fun DrawScope.drawGround(groundTop: Float) {
  drawRect(Color(0xFFE6B45F), topLeft = Offset(0f, groundTop), size = Size(size.width, size.height - groundTop))
  drawRect(Color(0xFF59B947), topLeft = Offset(0f, groundTop), size = Size(size.width, 16.dp.toPx()))
  val stripeWidth = 28.dp.toPx()
  var x = -stripeWidth
  while (x < size.width + stripeWidth) {
    drawLine(
      color = Color(0x552F7F30),
      start = Offset(x, groundTop + 18.dp.toPx()),
      end = Offset(x + stripeWidth, size.height),
      strokeWidth = 5.dp.toPx(),
    )
    x += stripeWidth
  }
}

private fun DrawScope.drawBird(world: GameWorld) {
  val center = Offset(toPxX(birdX()), toPxY(world.birdY))
  val radius = birdRadius() * minOf(size.width, size.height)
  val rotation = (world.birdVelocity * 55f).coerceIn(-24f, 36f)

  rotate(degrees = rotation, pivot = center) {
    drawCircle(Color(0xFFFFD23F), radius = radius, center = center)
    drawCircle(Color(0xFFFFA928), radius = radius * 0.62f, center = center + Offset(-radius * 0.45f, radius * 0.18f))
    drawOval(
      color = Color(0xFFFFE889),
      topLeft = center + Offset(-radius * 0.95f, -radius * 0.08f),
      size = Size(radius * 1.1f, radius * 0.62f),
    )

    val beak = Path().apply {
      moveTo(center.x + radius * 0.72f, center.y - radius * 0.14f)
      lineTo(center.x + radius * 1.45f, center.y + radius * 0.08f)
      lineTo(center.x + radius * 0.72f, center.y + radius * 0.34f)
      close()
    }
    drawPath(beak, Color(0xFFFF7A32))

    drawCircle(Color.White, radius = radius * 0.28f, center = center + Offset(radius * 0.33f, -radius * 0.30f))
    drawCircle(Color(0xFF17313F), radius = radius * 0.12f, center = center + Offset(radius * 0.42f, -radius * 0.28f))
  }
}

private fun DrawScope.toPxX(value: Float): Float = value * size.width

private fun DrawScope.toPxY(value: Float): Float = value * size.height

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
  FlappyBirdCloneTheme { MainScreen() }
}

@Preview(showBackground = true, widthDp = 340, heightDp = 720)
@Composable
fun MainScreenPortraitPreview() {
  FlappyBirdCloneTheme { MainScreen() }
}
