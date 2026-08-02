package com.raachi.memory.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.raachi.memory.core.designsystem.theme.RaachiMemoryTheme
import com.raachi.memory.feature.settings.AboutScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutAndThemeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun aboutScreen_showsProductVersionAndMission() {
        composeRule.setContent {
            RaachiMemoryTheme(darkTheme = false) {
                AboutScreen(onBack = {})
            }
        }

        composeRule.onNodeWithText("About Raachi Memory").assertIsDisplayed()
        composeRule.onNodeWithText("v1.0.0 Stable").assertIsDisplayed()
        composeRule.onNodeWithText("Our Mission").assertIsDisplayed()
    }

    @Test
    fun darkTheme_resolvesStableBackground() {
        var background = Color.Unspecified
        composeRule.setContent {
            RaachiMemoryTheme(darkTheme = true) {
                val color = MaterialTheme.colorScheme.background
                SideEffect { background = color }
            }
        }
        composeRule.runOnIdle { assertEquals(Color(0xFF121216), background) }
    }

    @Test
    fun lightTheme_resolvesStableBackground() {
        var background = Color.Unspecified
        composeRule.setContent {
            RaachiMemoryTheme(darkTheme = false) {
                val color = MaterialTheme.colorScheme.background
                SideEffect { background = color }
            }
        }
        composeRule.runOnIdle { assertEquals(Color(0xFFFCF9FF), background) }
    }
}
