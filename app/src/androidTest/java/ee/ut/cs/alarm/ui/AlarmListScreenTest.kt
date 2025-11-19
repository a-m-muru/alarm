package ee.ut.cs.alarm.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ee.ut.cs.alarm.ui.components.EditAlarmDialogTestTags
import ee.ut.cs.alarm.ui.components.SetTimeSemanticsKey
import ee.ut.cs.alarm.ui.helpers.TestAlarmRepository
import ee.ut.cs.alarm.ui.helpers.TestAlarmScheduler
import ee.ut.cs.alarm.ui.screens.AlarmListScreen
import ee.ut.cs.alarm.ui.theme.AlarmTheme
import ee.ut.cs.alarm.ui.viewmodel.AlarmListViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlarmListScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var repository: TestAlarmRepository
    private lateinit var alarmScheduler: TestAlarmScheduler
    private lateinit var viewModel: AlarmListViewModel

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        repository = TestAlarmRepository()
        alarmScheduler = TestAlarmScheduler(context)
        viewModel = AlarmListViewModel(repository)
    }

    @Test
    fun openingAddAlarmDialog_showsDialogFields() {
        setContent()

        composeTestRule.onNodeWithText("No alarms set", substring = true, ignoreCase = true)
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Add").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Add or edit alarm").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alarm label").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mon").assertIsDisplayed()
    }

    @Test
    fun savingDefaultAlarm_displaysAlarmCardInList() {
        setContent()

        composeTestRule.onNodeWithContentDescription("Add").performClick()
        composeTestRule.onNodeWithText("Save").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.items.value.isNotEmpty()
        }

        composeTestRule.onNodeWithText("00:00").assertIsDisplayed()
    }

    @Test
    fun savingAlarm_withCustomTime_displaysCustomTime() {
        setContent()

        composeTestRule.onNodeWithContentDescription("Add").performClick()
        setTime(10, 30)
        composeTestRule.onNodeWithText("Save").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.items.value.any { it.time == (10 * 3600 + 30 * 60).toUInt() }
        }

        composeTestRule.onNodeWithText("10:30").assertIsDisplayed()
    }

    private fun setContent() {
        composeTestRule.setContent {
            AlarmTheme {
                AlarmListScreen(
                    onNavigate = {},
                    vm = viewModel,
                    alarmScheduler = alarmScheduler
                )
            }
        }
    }

    private fun setTime(hour: Int, minute: Int) {
        val semanticsNode = composeTestRule
            .onNodeWithTag(EditAlarmDialogTestTags.TIME_PICKER, useUnmergedTree = true)
            .fetchSemanticsNode()
        val setTimeAction = semanticsNode.config[SetTimeSemanticsKey]
        composeTestRule.runOnIdle {
            setTimeAction(hour, minute)
        }
    }
}

