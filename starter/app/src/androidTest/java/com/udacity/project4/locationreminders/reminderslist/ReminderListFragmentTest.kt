package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.koin.dsl.module
import com.udacity.project4.locationreminders.data.ERROR_MESSAGE_
import com.udacity.project4.locationreminders.data.local.LocalDB
import org.junit.After
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.mockito.Mockito
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var dataSource: FakeAndroidDataSource
    private lateinit var appContext: Application

    @Before
    fun setup() {
        stopKoin()

        dataSource = FakeAndroidDataSource()
        appContext = getApplicationContext()

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    get() as ReminderDataSource
                )
            }
            single(override = true) {
                dataSource as ReminderDataSource
            }
            single { LocalDB.createRemindersDao(appContext) }
        }

        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun saveReminder_displayedInUi() = runBlockingTest {
        // GIVEN - Add reminder to the DB
        val reminder = ReminderDTO("Time to play!", "Bouncy house is fun", "location", 100.0, 100.0)
        dataSource.saveReminder(reminder)

        // WHEN - RemindersList fragment launched to display list of reminders
        launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)

        // THEN - list containing one reminder is shown
        // make sure that title/description/location are shown and correct
        onView(withId(R.id.reminderCardView)).check(matches(isDisplayed()))
        onView(withId(R.id.title)).check(matches(withText("Time to play!")))
        onView(withId(R.id.description)).check(matches(withText("Bouncy house is fun")))
    }

    @Test
    fun emptyRemindersList_noDataShown() = runBlockingTest {
        // GIVEN - empty reminders list
        // WHEN - RemindersList fragment launched
        launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)

        // THEN - "No data" message is shown
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun saveAndDeleteReminders_noDataShown() = runBlockingTest {
        // GIVEN - Add reminder to the DB
        val reminder = ReminderDTO("Time to play!", "Bouncy house is fun", "location", 100.0, 100.0)
        dataSource.saveReminder(reminder)

        // WHEN - RemindersList fragment launched to display list of reminders; call deleteAll and refreshList
        launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)
        dataSource.deleteAllReminders()
        onView(withId(R.id.refreshLayout)).perform(swipeDown())

        // THEN - "No data" message is shown
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun getReminders_dataError_snackbarIsShown() = runBlockingTest {
        // GIVEN - fake data source returns error
        dataSource.shouldReturnError = true

        // WHEN - fragment launched
        launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)

        // THEN - error snackbar is shown
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(ERROR_MESSAGE_)))
    }

    @Test
    fun onFabClick_navigateToSaveReminderFragment() = runBlockingTest {
        // GIVEN - on RemindersListFragment screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - FAB clicked
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN - verify user navigated to SaveReminderFragment
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

}