package com.example.flappybirdclone.ui.main

import junit.framework.TestCase.assertEquals
import org.junit.Test

class HighScoreTrackerTest {
  @Test
  fun existingHighScore_isLoadedFromStore() {
    val tracker = HighScoreTracker(FakeHighScoreStore(initialScore = 4))

    assertEquals(4, tracker.highScore)
  }

  @Test
  fun higherScore_isSaved() {
    val store = FakeHighScoreStore(initialScore = 4)
    val tracker = HighScoreTracker(store)

    val result = tracker.record(7)

    assertEquals(7, result)
    assertEquals(7, store.savedScore)
  }

  @Test
  fun lowerScore_doesNotReplaceHighScore() {
    val store = FakeHighScoreStore(initialScore = 7)
    val tracker = HighScoreTracker(store)

    val result = tracker.record(3)

    assertEquals(7, result)
    assertEquals(null, store.savedScore)
  }
}

private class FakeHighScoreStore(initialScore: Int) : HighScoreStore {
  private val initialScore = initialScore
  var savedScore: Int? = null
    private set

  override fun readHighScore(): Int = initialScore

  override fun writeHighScore(score: Int) {
    savedScore = score
  }
}
