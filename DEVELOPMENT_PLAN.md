# Flappy Bird Clone Development Plan

## Goal

Build a single-screen Android Flappy Bird clone in Jetpack Compose. The game should start immediately, use tap input to flap, scroll pipe obstacles, track score, detect collisions, and support retry after game over.

## Technical Approach

1. Start from the Android CLI `empty-activity` Compose template.
2. Keep Navigation 3 only as the app shell, with one main game route.
3. Render the game with Compose `Canvas`:
   - Procedural bird sprite.
   - Procedural pipe obstacles.
   - Sky, clouds, and ground.
   - Lightweight HUD composables for title, score, and state prompts.
4. Use a `withFrameNanos` loop for frame updates.
5. Keep game physics in a Compose-independent engine:
   - Immutable `GameWorld`.
   - `Ready`, `Playing`, and `GameOver` phases.
   - Tap transition and flap velocity.
   - Gravity, pipe movement, scoring, collision, and pipe replenishment.
6. Avoid external sprite dependencies unless art requirements expand. The current version draws sprites directly on Canvas, so there are no licensing or asset-loading concerns.
7. Test the non-UI game engine with local JVM unit tests.
8. Test the initial Compose screen with an instrumentation test for visible ready state and game field semantics.

## Build And Verification

1. Build debug APK with Gradle.
2. Run local unit tests.
3. Run instrumentation tests on an emulator when a device is available.
4. Optional polish pass:
   - Add sound effects.
   - Persist high score with DataStore.
   - Add difficulty ramping.
   - Add custom launcher icon art.
