package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CoachTactics", appName)
  }

  @Test
  fun `verify tactical data models and fast change engine`() {
    val defaultDrills = com.example.data.SampleTacticsData.defaultDrills
    assertEquals(3, defaultDrills.size)

    val wingDrill = defaultDrills.first()
    assertEquals("Overlapping Wing Delivery & Box Attack", wingDrill.title)
    assertEquals(4, wingDrill.phases.size)

    val service = com.example.data.GeminiTacticsService()
    val updatedDrill = service.applyFastChange(wingDrill, "+1 Defender Press")
    assertEquals(true, updatedDrill.title.contains("+1 Def"))

    val highTempoDrill = service.applyFastChange(wingDrill, "High Tempo 1-Touch")
    assertEquals(true, highTempoDrill.title.contains("High Tempo"))
  }
}
