package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.ERROR_MESSAGE
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Use a fake data source to be injected into the viewmodel
    private lateinit var dataSource: FakeDataSource

    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        // Initialize FirebaseApp
        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().targetContext)

        // Initialize with 3 reminders
        dataSource = FakeDataSource()
        val reminder1 = ReminderDTO("title1", "description1", "location1", 100.0, 100.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 200.0, 200.0)
        val reminder3 = ReminderDTO("title3", "description3", "location3", 300.0, 300.0)
        dataSource.addReminders(reminder1, reminder2, reminder3)

        remindersListViewModel = RemindersListViewModel(dataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }


    @Test
    fun loadReminders_success_remindersListHasData() {
        // GIVEN - a fresh viewModel
        dataSource.isResultSuccess = true

        // WHEN - loading reminders
        remindersListViewModel.loadReminders()

        // THEN - reminders list contains data
        val value = remindersListViewModel.remindersList.getOrAwaitValue()
        assertThat(value.isNullOrEmpty(), `is`(false))
    }

    @Test
    fun loadReminders_success_showNoDataIsFalse() {
        // GIVEN - a fresh viewModel
        dataSource.isResultSuccess = true

        // WHEN - loading reminders
        remindersListViewModel.loadReminders()

        // THEN - showNoData is false
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminders_error_showNoDataIsTrue() {
        // GIVEN - a fresh viewModel
        dataSource.isResultSuccess = false

        // WHEN - loading reminders
        remindersListViewModel.loadReminders()

        // THEN - showNoData is false
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadReminders_error_snackbarUpdated() {
        // GIVEN - a fresh viewModel
        dataSource.isResultSuccess = false

        // WHEN - loading reminders
        remindersListViewModel.loadReminders()

        // THEN - snackbar value updated
        val snackbarText = remindersListViewModel.showSnackBar.getOrAwaitValue()
        assertThat(snackbarText, `is`(ERROR_MESSAGE))
    }

    @Test
    fun loadReminders_showLoadingIsFalse() {
        // GIVEN - a fresh viewModel
        dataSource.isResultSuccess = false

        // WHEN - loading reminders
        remindersListViewModel.loadReminders()

        // THEN - showLoading is false
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

}