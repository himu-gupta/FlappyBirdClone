package com.example.flappybirdclone.ui.main

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class GameEngineTest {
  private val phoneAspectRatio = 1080f / 2400f

  @Test
  fun tapFromReady_startsGameWithUpwardVelocity() {
    val world = freshWorld().onTap()

    assertEquals(GamePhase.Playing, world.phase)
    assertTrue(world.birdVelocity < 0f)
  }

  @Test
  fun tickWhilePlaying_movesPipesLeft() {
    val world = freshWorld().onTap()
    val initialPipeX = world.pipes.first().x

    val next = world.tick(0.5f)

    assertTrue(next.pipes.first().x < initialPipeX)
  }

  @Test
  fun passingPipe_incrementsScoreOnce() {
    val world =
      freshWorld()
        .copy(
          phase = GamePhase.Playing,
          pipes = listOf(Pipe(id = 99, x = birdX() - pipeWidth() + 0.01f, gapCenter = 0.5f)),
          nextPipeId = 100,
        )

    val scored = world.tick(0.1f)
    val scoredAgain = scored.tick(0.1f)

    assertEquals(1, scored.score)
    assertEquals(1, scoredAgain.score)
  }

  @Test
  fun birdVisiblyClearOfPipe_doesNotCollideOnPortraitScreen() {
    val gapCenter = 0.5f
    val gapTop = gapCenter - pipeGap() / 2f
    val world =
      freshWorld().copy(
        phase = GamePhase.Playing,
        birdY = gapTop + 0.015f,
        pipes = listOf(Pipe(id = 99, x = birdX() - pipeWidth() / 2f, gapCenter = gapCenter)),
        nextPipeId = 100,
      )

    val next = world.tick(deltaSeconds = 0f, viewportAspectRatio = phoneAspectRatio)

    assertEquals(GamePhase.Playing, next.phase)
  }

  @Test
  fun birdTouchingPipe_collidesOnPortraitScreen() {
    val gapCenter = 0.5f
    val gapTop = gapCenter - pipeGap() / 2f
    val world =
      freshWorld().copy(
        phase = GamePhase.Playing,
        birdY = gapTop + 0.010f,
        pipes = listOf(Pipe(id = 99, x = birdX() - pipeWidth() / 2f, gapCenter = gapCenter)),
        nextPipeId = 100,
      )

    val next = world.tick(deltaSeconds = 0f, viewportAspectRatio = phoneAspectRatio)

    assertEquals(GamePhase.GameOver, next.phase)
  }
}
