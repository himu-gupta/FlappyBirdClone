package com.example.flappybirdclone.ui.main

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

interface HighScoreStore {
  fun readHighScore(): Int

  fun writeHighScore(score: Int)
}

class HighScoreTracker(private val store: HighScoreStore) {
  var highScore: Int = store.readHighScore().coerceAtLeast(0)
    private set

  fun record(score: Int): Int {
    if (score > highScore) {
      highScore = score
      store.writeHighScore(score)
    }
    return highScore
  }
}

private class SharedPreferencesHighScoreStore(context: Context) : HighScoreStore {
  private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

  override fun readHighScore(): Int = preferences.getInt(HIGH_SCORE_KEY, 0)

  override fun writeHighScore(score: Int) {
    preferences.edit().putInt(HIGH_SCORE_KEY, score).apply()
  }

  private companion object {
    const val PREFERENCES_NAME = "flappy_bird_progress"
    const val HIGH_SCORE_KEY = "high_score"
  }
}

@Composable
fun rememberHighScoreStore(): HighScoreStore {
  val applicationContext = LocalContext.current.applicationContext
  return remember(applicationContext) { SharedPreferencesHighScoreStore(applicationContext) }
}
