package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.google.firebase.FirebaseApp
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class SaveReminderViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Use a fake data source to be injected into the viewmodel
    private lateinit var dataSource: FakeDataSource

    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        // Initialize FirebaseApp
        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().targetContext)

        // Initialize with 3 reminders
        dataSource = FakeDataSource()

        saveReminderViewModel = SaveReminderViewModel(dataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun onClear_liveDataFieldsAreNull() {
        // GIVEN - a fresh viewModel
        // WHEN - viewModel onClear
        saveReminderViewModel.onClear()

        // THEN - liveData fields are empty
        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(
            saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(),
            `is`(nullValue())
        )
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), `is`(nullValue()))
    }

    @Test
    fun validateAndSaveReminder_titleIsEmpty_snackbarUpdated() {
        // GIVEN - a fresh viewModel
        // WHEN - validateEnteredData
        val reminderDataItem = ReminderDataItem("", "description", "location", 100.0, 100.0)
        saveReminderViewModel.validateAndSaveReminder(reminderDataItem)

        // THEN snackbar value updated
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun validateAndSaveReminder_descriptionIsEmpty_snackbarUpdated() {
        // GIVEN - a fresh viewModel
        // WHEN - validateEnteredData
        val reminderDataItem = ReminderDataItem("title", "", "location", 100.0, 100.0)
        saveReminderViewModel.validateAndSaveReminder(reminderDataItem)

        // THEN snackbar value updated
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_description))
    }

    @Test
    fun validateAndSaveReminder_locationIsEmpty_snackbarUpdated() {
        // GIVEN - a fresh viewModel
        // WHEN - validateEnteredData
        val reminderDataItem = ReminderDataItem("title", "description", "", 100.0, 100.0)
        saveReminderViewModel.validateAndSaveReminder(reminderDataItem)

        // THEN snackbar value updated
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }

    @Test
    fun validateAndSaveReminder_liveDataUpdated() {
        // GIVEN - a fresh viewModel
        // WHEN - validateEnteredData and getReminder
        val reminderDataItem = ReminderDataItem("title", "description", "location", 100.0, 100.0)
        saveReminderViewModel.validateAndSaveReminder(reminderDataItem)

        // THEN - LiveData updated
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(saveReminderViewModel.showToastInt.getOrAwaitValue(), `is`(R.string.reminder_saved))
        assertEquals(NavigationCommand.Back, saveReminderViewModel.navigationCommand.getOrAwaitValue())
    }

    @Test
    fun onLocationSelected_liveDataFieldsUpdated() {
        // GIVEN - a fresh viewModel
        // WHEN - onLocationSelected
        val latLng = LatLng(100.0, -100.0)
        val poi = PointOfInterest(latLng, "placeId", "name")
        saveReminderViewModel.onLocationSelected(poi)

        // THEN - liveData fields updated
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue(), `is`(poi))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(poi.name))
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), `is`(poi.latLng.latitude))
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), `is`(poi.latLng.longitude))
        assertEquals(NavigationCommand.Back, saveReminderViewModel.navigationCommand.getOrAwaitValue())
    }

}