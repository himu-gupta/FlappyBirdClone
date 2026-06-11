package com.example.flappybirdclone.ui.main

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class GameEngineTest {
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
}
