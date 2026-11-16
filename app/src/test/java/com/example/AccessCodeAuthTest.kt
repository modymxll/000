package com.example

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BubblesLocalDatabase
import com.example.data.local.LocalWorkspaceRepository
import com.example.data.session.ActiveSession
import com.example.data.session.SessionPreferences
import com.example.ui.auth.AuthScreen
import com.example.ui.i18n.ArabicStrings
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.BubblesDeliveryTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.example.data.validation.SaudiPhoneUtils
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AccessCodeAuthTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var db: BubblesLocalDatabase
    private lateinit var workspace: LocalWorkspaceRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, BubblesLocalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        workspace = LocalWorkspaceRepository(db.workspaceDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `default admin code is 605790 and session switches roles`() = runBlocking {
        val prefs = SessionPreferences(context)
        val initial = prefs.sessionFlow.first()
        assertFalse(initial.isLoggedIn)
        assertEquals(ActiveSession.DEFAULT_ADMIN_CODE, initial.adminAccessCode)
        assertEquals("605790", initial.adminAccessCode)
        assertEquals("admin", initial.adminUsername)

        prefs.loginAsAdmin("الإدارة")
        val adminSession = prefs.sessionFlow.first()
        assertTrue(adminSession.isLoggedIn)
        assertEquals("ADMIN", adminSession.role)

        prefs.loginAsDriver("drv_1", "سالم الدوسري")
        val driverSession = prefs.sessionFlow.first()
        assertTrue(driverSession.isLoggedIn)
        assertEquals("DRIVER", driverSession.role)
        assertEquals("drv_1", driverSession.driverId)

        prefs.logout()
        assertFalse(prefs.sessionFlow.first().isLoggedIn)
    }

    @Test
    fun `creating driver assigns unique access code and supports credential lookup`() = runBlocking {
        val res = workspace.createDriver(
            actorId = "admin_local",
            actorName = "الإدارة",
            name = "خالد القحطاني",
            username = "khaled",
            phone = "0501234567",
            email = null,
            requestedCode = "112233",
            status = "ACTIVE"
        )
        assertTrue(res.isSuccess)
        val (driverId, code) = res.getOrThrow()
        assertEquals("112233", code)

        val byCode = workspace.findDriverByAccessCode("112233")
        assertNotNull(byCode)
        assertEquals(driverId, byCode?.id)

        val byCreds = workspace.findDriverByCredentials("khaled", "112233")
        assertNotNull(byCreds)
        assertEquals("خالد القحطاني", byCreds?.name)
    }

    @Test
    fun `saudi phone utils normalizes and validates all standard and Arabic-digit formats`() {
        val validInputs = listOf(
            "0501234567" to "+966501234567",
            "501234567" to "+966501234567",
            "+966501234567" to "+966501234567",
            "+9660501234567" to "+966501234567",
            "00966501234567" to "+966501234567",
            "966501234567" to "+966501234567",
            "٠٥٠١٢٣٤٥٦٧" to "+966501234567",
            "+٩٦٦٥٠١٢٣٤٥٦٧" to "+966501234567",
            "055 123 4567" to "+966551234567",
            "0112345678" to "+966112345678",
            "920012345" to "920012345",
            "8001234567" to "8001234567"
        )
        validInputs.forEach { (raw, expected) ->
            assertTrue("Expected valid for $raw", SaudiPhoneUtils.isValid(raw))
            assertEquals("Expected normalized $expected for $raw", expected, SaudiPhoneUtils.normalize(raw))
        }

        val invalidInputs = listOf("", "123", "05012345", "+96650501234567", "+12025550123")
        invalidInputs.forEach { raw ->
            assertFalse("Expected invalid for $raw", SaudiPhoneUtils.isValid(raw))
        }
    }

    @Test
    fun `auth keypad auto submits 605790 admin code without Google sign in`() {
        var submittedCode: String? = null

        composeRule.setContent {
            BubblesDeliveryTheme {
                CompositionLocalProvider(LocalAppStrings provides ArabicStrings) {
                    AuthScreen(
                        onToggleLanguage = {},
                        onLoginWithCode = { code ->
                            submittedCode = code
                            Result.success(Unit)
                        }
                    )
                }
            }
        }

        "605790".forEach { digit ->
            composeRule.onNodeWithTag("pin_key_$digit")
                .performScrollTo()
                .performClick()
        }
        composeRule.waitUntil(timeoutMillis = 3000L) { submittedCode != null }
        assertEquals("605790", submittedCode)
    }
}
