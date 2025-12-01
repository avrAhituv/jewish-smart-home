package com.jewishhome.app

import com.jewishhome.app.util.InactivityHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InactivityHandlerTest {

    private lateinit var testScope: TestScope
    private lateinit var inactivityHandler: InactivityHandler

    @Before
    fun setup() {
        testScope = TestScope(StandardTestDispatcher())
        inactivityHandler = InactivityHandler(testScope, defaultTimeoutMinutes = 1)
    }

    @Test
    fun `initial state should not show screensaver`() = testScope.runTest {
        assertFalse(inactivityHandler.shouldShowScreensaver.value)
    }

    @Test
    fun `screensaver should show after timeout`() = testScope.runTest {
        inactivityHandler.start()
        advanceTimeBy(61_000) // 61 seconds > 1 minute

        assertTrue(inactivityHandler.shouldShowScreensaver.value)
    }

    @Test
    fun `user activity should reset timer`() = testScope.runTest {
        inactivityHandler.start()
        advanceTimeBy(30_000) // 30 seconds

        inactivityHandler.onUserActivity()
        advanceTimeBy(30_000) // Another 30 seconds (total 60 but reset at 30)

        assertFalse(inactivityHandler.shouldShowScreensaver.value)
    }

    @Test
    fun `dismiss should hide screensaver and reset timer`() = testScope.runTest {
        inactivityHandler.start()
        advanceTimeBy(61_000)
        assertTrue(inactivityHandler.shouldShowScreensaver.value)

        inactivityHandler.dismissScreensaver()
        assertFalse(inactivityHandler.shouldShowScreensaver.value)
    }

    @Test
    fun `stop should cancel timer`() = testScope.runTest {
        inactivityHandler.start()
        inactivityHandler.stop()
        advanceTimeBy(120_000) // 2 minutes

        assertFalse(inactivityHandler.shouldShowScreensaver.value)
    }

    @Test
    fun `setEnabled false should disable screensaver`() = testScope.runTest {
        inactivityHandler.setEnabled(false)
        inactivityHandler.start()
        advanceTimeBy(120_000)

        assertFalse(inactivityHandler.shouldShowScreensaver.value)
    }

    @Test
    fun `setTimeoutMinutes should change timeout`() = testScope.runTest {
        inactivityHandler.setTimeoutMinutes(2) // 2 minutes
        inactivityHandler.start()
        advanceTimeBy(61_000) // 1 minute

        assertFalse(inactivityHandler.shouldShowScreensaver.value)

        advanceTimeBy(60_000) // Another minute (total 2 minutes)
        assertTrue(inactivityHandler.shouldShowScreensaver.value)
    }
}
