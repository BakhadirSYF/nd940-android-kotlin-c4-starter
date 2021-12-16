package com.udacity.project4

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import io.mockk.every
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import android.view.inputmethod.InputMethodManager
import androidx.test.espresso.action.ViewActions.*
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import com.udacity.project4.util.ToastMatcher
import io.mockk.spyk


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: FakeAndroidDataSource
    private lateinit var appContext: Application
    private lateinit var imm: InputMethodManager
    private lateinit var viewModelSpy: SaveReminderViewModel

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun setup() {
        stopKoin()//stop the original app koin

        appContext = getApplicationContext()
        repository = FakeAndroidDataSource()
        imm = appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        viewModelSpy = spyk(SaveReminderViewModel(repository))

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    get() as ReminderDataSource
                )
            }
            single(override = true) {
                viewModelSpy
            }
            single(override = true) {
                repository as ReminderDataSource
            }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    private fun setupSpyMocks() {
        var latLng = LatLng(37.3752, -122.0318)
        var poi = PointOfInterest(latLng, null, "Point of interest")

        every { viewModelSpy.selectedPOI.value } returns poi
        every { viewModelSpy.reminderSelectedLocationStr.value } returns poi.name
        every { viewModelSpy.latitude.value } returns poi.latLng.latitude
        every { viewModelSpy.longitude.value } returns poi.latLng.longitude
        every { viewModelSpy.isLocationSelected() } returns true
    }

    @Test
    fun endToEndTest() = runBlocking {
        val activityScenario: ActivityScenario<RemindersActivity> =
            ActivityScenario.launch(RemindersActivity::class.java)

        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Verify "No data" displayed
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        // FAB click
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Verify Save Reminder fields are empty
        onView(withId(R.id.reminderTitle)).check(matches(withText("")))
        onView(withId(R.id.reminderDescription)).check(matches(withText("")))
        onView(withId(R.id.selectedLocation)).check(matches(withText("")))

        // Enter title and description
        onView(withId(R.id.reminderTitle)).perform(click(), replaceText("Location title"))
        onView(withId(R.id.reminderDescription)).perform(
            click(),
            replaceText("Location description")
        )

        // Hide soft keyboard
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        // Setup spy mocks
        setupSpyMocks()

        // Save FAB click
        onView(withId(R.id.saveReminder)).perform(click())

        // Toast is shown
        onView(withText(R.string.geofence_added)).inRoot(ToastMatcher())
            .check(matches(isDisplayed()))

        // Returns back to Reminders List, verify correct reminder is shown
        onView(withId(R.id.reminderCardView)).check(matches(isDisplayed()))
        onView(withId(R.id.title)).check(matches(withText("Location title")))
        onView(withId(R.id.description)).check(matches(withText("Location description")))

        // Make sure the activity is closed.
        activityScenario.close()
    }
}
