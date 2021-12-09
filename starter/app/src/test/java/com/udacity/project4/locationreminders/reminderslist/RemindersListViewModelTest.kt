package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

//@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects

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
        // Initialize with 3 reminders
        dataSource = FakeDataSource()
        val reminder1 = ReminderDTO("title1", "description1", "location1", 100.0, 100.0)
        val reminder2 = ReminderDTO("title2", "description2", "location2", 200.0, 200.0)
        val reminder3 = ReminderDTO("title3", "description3", "location3", 300.0, 300.0)
        dataSource.addReminders(reminder1, reminder2, reminder3)

//        remindersListViewModel = RemindersListViewModel(null, dataSource)
    }

}