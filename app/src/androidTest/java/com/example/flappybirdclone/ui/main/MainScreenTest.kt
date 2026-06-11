package com.example.flappybirdclone.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [MainScreen]. */
class MainScreenTest {

  private val highScoreStore = FakeHighScoreStore(initialScore = 7)

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent { MainScreen(highScoreStore = highScoreStore) }
  }

  @Test
  fun readyState_isDisplayed() {
    composeTestRule.onNodeWithText("Flappy Bird").assertExists()
    composeTestRule.onNodeWithText("Ready").assertExists()
    composeTestRule.onNodeWithText("Tap to flap").assertExists()
    composeTestRule.onNodeWithText("SCORE").assertExists()
    composeTestRule.onNodeWithText("BEST").assertExists()
    composeTestRule.onNodeWithText("7").assertExists()
    composeTestRule.onNodeWithTag("flappy_game").assertContentDescriptionEquals("Flappy Bird game field")
  }
}

private class FakeHighScoreStore(private val initialScore: Int) : HighScoreStore {
  override fun readHighScore(): Int = initialScore

  override fun writeHighScore(score: Int) = Unit
}
