package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AtmosphereTheme
import com.example.model.FocusTimerState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Horizon", appName)
  }

  @Test
  fun `focus timer progress calculates accurately`() {
    val state = FocusTimerState(totalSeconds = 100, remainingSeconds = 25)
    assertEquals(0.75f, state.progress, 0.001f)
  }

  @Test
  fun `atmosphere theme lookup falls back cleanly`() {
    assertEquals(AtmosphereTheme.AURORA_TEAL, AtmosphereTheme.fromId("aurora_teal"))
    assertEquals(AtmosphereTheme.OBSIDIAN_AMBER, AtmosphereTheme.fromId("unknown"))
  }
}
