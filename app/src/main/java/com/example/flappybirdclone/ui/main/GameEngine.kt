package com.example.flappybirdclone.ui.main

private const val BirdX = 0.28f
private const val BirdStartY = 0.44f
private const val BirdRadius = 0.035f
private const val BirdHitboxScale = 0.85f
private const val Gravity = 1.75f
private const val FlapVelocity = -0.62f
private const val PipeSpeed = 0.32f
private const val PipeWidth = 0.14f
private const val PipeGap = 0.27f
private const val PipeSpacing = 0.48f
private const val GroundY = 0.88f
private const val MaxDeltaSeconds = 0.033f

enum class GamePhase {
  Ready,
  Playing,
  GameOver,
}

data class Pipe(
  val id: Int,
  val x: Float,
  val gapCenter: Float,
  val scored: Boolean = false,
)

data class GameWorld(
  val phase: GamePhase = GamePhase.Ready,
  val birdY: Float = BirdStartY,
  val birdVelocity: Float = 0f,
  val pipes: List<Pipe> = initialPipes(),
  val score: Int = 0,
  val nextPipeId: Int = 3,
)

fun GameWorld.onTap(): GameWorld =
  when (phase) {
    GamePhase.Ready -> freshWorld().copy(phase = GamePhase.Playing, birdVelocity = FlapVelocity)
    GamePhase.Playing -> copy(birdVelocity = FlapVelocity)
    GamePhase.GameOver -> freshWorld().copy(phase = GamePhase.Playing, birdVelocity = FlapVelocity)
  }

fun GameWorld.tick(deltaSeconds: Float, viewportAspectRatio: Float = 1f): GameWorld {
  if (phase != GamePhase.Playing) return this

  val dt = deltaSeconds.coerceIn(0f, MaxDeltaSeconds)
  val nextBirdVelocity = birdVelocity + Gravity * dt
  val nextBirdY = birdY + nextBirdVelocity * dt
  val movedPipes = pipes.map { it.copy(x = it.x - PipeSpeed * dt) }.filter { it.x + PipeWidth > -0.05f }
  val replenishedPipes = replenishPipes(movedPipes, nextPipeId)
  val scoredPipes =
    replenishedPipes.pipes.map { pipe ->
      if (!pipe.scored && pipe.x + PipeWidth < BirdX) pipe.copy(scored = true) else pipe
    }
  val scoreDelta = scoredPipes.count { pipe ->
    val before = replenishedPipes.pipes.first { it.id == pipe.id }
    !before.scored && pipe.scored
  }

  val next = copy(
    birdY = nextBirdY,
    birdVelocity = nextBirdVelocity,
    pipes = scoredPipes,
    score = score + scoreDelta,
    nextPipeId = replenishedPipes.nextPipeId,
  )

  return if (next.hasCollision(viewportAspectRatio)) next.copy(phase = GamePhase.GameOver) else next
}

internal fun freshWorld(): GameWorld = GameWorld()

internal fun birdX() = BirdX

internal fun birdRadius() = BirdRadius

internal fun pipeWidth() = PipeWidth

internal fun pipeGap() = PipeGap

internal fun groundY() = GroundY

private data class PipeReplenishResult(val pipes: List<Pipe>, val nextPipeId: Int)

private fun initialPipes(): List<Pipe> =
  listOf(Pipe(id = 1, x = 1.12f, gapCenter = gapCenterFor(1)), Pipe(id = 2, x = 1.60f, gapCenter = gapCenterFor(2)))

private fun replenishPipes(currentPipes: List<Pipe>, startingNextPipeId: Int): PipeReplenishResult {
  val pipes = currentPipes.toMutableList()
  var nextPipeId = startingNextPipeId
  while ((pipes.maxOfOrNull { it.x } ?: 0f) < 1.12f) {
    pipes += Pipe(id = nextPipeId, x = (pipes.maxOfOrNull { it.x } ?: 1.12f) + PipeSpacing, gapCenter = gapCenterFor(nextPipeId))
    nextPipeId++
  }
  return PipeReplenishResult(pipes = pipes, nextPipeId = nextPipeId)
}

private fun gapCenterFor(pipeId: Int): Float {
  val wave = ((pipeId * 37) % 100) / 100f
  return 0.28f + wave * 0.34f
}

private fun GameWorld.hasCollision(viewportAspectRatio: Float): Boolean {
  val hitbox = birdHitbox(viewportAspectRatio)
  if (birdY - hitbox.radiusY <= 0f || birdY + hitbox.radiusY >= GroundY) return true

  pipes.forEach { pipe ->
    if (ellipseIntersectsRect(BirdX, birdY, hitbox, pipe.x, 0f, PipeWidth, pipe.gapCenter - PipeGap / 2f)) return true
    if (ellipseIntersectsRect(BirdX, birdY, hitbox, pipe.x, pipe.gapCenter + PipeGap / 2f, PipeWidth, GroundY - pipe.gapCenter)) return true
  }

  return false
}

private data class BirdHitbox(val radiusX: Float, val radiusY: Float)

private fun birdHitbox(viewportAspectRatio: Float): BirdHitbox {
  val aspectRatio = viewportAspectRatio.coerceAtLeast(0.01f)
  val radiusX = BirdRadius * minOf(1f, 1f / aspectRatio) * BirdHitboxScale
  val radiusY = BirdRadius * minOf(aspectRatio, 1f) * BirdHitboxScale
  return BirdHitbox(radiusX = radiusX, radiusY = radiusY)
}

private fun ellipseIntersectsRect(
  circleX: Float,
  circleY: Float,
  hitbox: BirdHitbox,
  rectX: Float,
  rectY: Float,
  rectWidth: Float,
  rectHeight: Float,
): Boolean {
  if (rectHeight <= 0f || rectWidth <= 0f) return false
  val closestX = circleX.coerceIn(rectX, rectX + rectWidth)
  val closestY = circleY.coerceIn(rectY, rectY + rectHeight)
  val dx = circleX - closestX
  val dy = circleY - closestY
  val normalizedX = dx / hitbox.radiusX
  val normalizedY = dy / hitbox.radiusY
  return normalizedX * normalizedX + normalizedY * normalizedY <= 1f
}
