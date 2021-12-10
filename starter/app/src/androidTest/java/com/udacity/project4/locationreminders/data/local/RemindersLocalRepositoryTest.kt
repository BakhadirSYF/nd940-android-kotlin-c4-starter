package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // Initialize FirebaseApp
        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().targetContext)

        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)

    }

    @After
    fun cleanUp() {
        database.close()
        stopKoin()
    }

    @Test
    fun saveReminder_getReminder_dataMatches() = runBlocking {
        // GIVEN - new reminder saved in database
        val newReminder = ReminderDTO("title", "description", "location", 100.0, -100.0)
        localDataSource.saveReminder(newReminder)

        // WHEN - reminder retrieved by id
        val result = localDataSource.getReminder(newReminder.id)

        // THEN - data retrieved matches saved
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.title, `is`(newReminder.title))
        assertThat(result.data.description, `is`(newReminder.description))
        assertThat(result.data.location, `is`(newReminder.location))
        assertThat(result.data.latitude, `is`(newReminder.latitude))
        assertThat(result.data.longitude, `is`(newReminder.longitude))
    }

    @Test
    fun saveReminders_getReminders_correctNumOfRemindersReturned() = runBlocking {
        // GIVEN - new reminders saved in database
        val newReminder1 = ReminderDTO("title1", "description1", "location1", 100.0, -100.0)
        localDataSource.saveReminder(newReminder1)
        val newReminder2 = ReminderDTO("title2", "description2", "location2", 110.0, -110.0)
        localDataSource.saveReminder(newReminder2)
        val newReminder3 = ReminderDTO("title3", "description3", "location3", 120.0, -120.0)
        localDataSource.saveReminder(newReminder3)

        // WHEN - getReminders is called
        val result = localDataSource.getReminders()

        // THEN - correct number of reminders returned
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.size, `is`(3))
    }

    @Test
    fun saveReminders_deleteAllReminders_databaseIsEmpty() = runBlocking {
        // GIVEN - new reminders saved in database
        val newReminder1 = ReminderDTO("title1", "description1", "location1", 100.0, -100.0)
        localDataSource.saveReminder(newReminder1)
        val newReminder2 = ReminderDTO("title2", "description2", "location2", 110.0, -110.0)
        localDataSource.saveReminder(newReminder2)
        val newReminder3 = ReminderDTO("title3", "description3", "location3", 120.0, -120.0)
        localDataSource.saveReminder(newReminder3)

        val result = localDataSource.getReminders()

        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.size, `is`(3))

        // WHEN - deleteAllReminders is called
        localDataSource.deleteAllReminders()

        // THEN - database is empty
        val afterDeleteResult = localDataSource.getReminders()
        assertThat(afterDeleteResult.succeeded, `is`(true))
        afterDeleteResult as Result.Success
        assertThat(afterDeleteResult.data.size, `is`(0))
    }
}