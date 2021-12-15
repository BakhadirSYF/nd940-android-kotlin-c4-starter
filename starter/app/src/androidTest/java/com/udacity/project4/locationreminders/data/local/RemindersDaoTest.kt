package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // Initialize FirebaseApp
        FirebaseApp.initializeApp(InstrumentationRegistry.getInstrumentation().targetContext)

        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
        stopKoin()
    }

    @Test
    fun saveReminderAndGetById() = runBlockingTest {
        // GIVEN - save reminder
        val reminder = ReminderDTO("title", "description", "location", 100.0, -100.0)
        database.reminderDao().saveReminder(reminder)

        // WHEN - get reminder by id from database
        val result = database.reminderDao().getReminderById(reminder.id)

        // THEN - retrieved data contains expected values
        assertThat<ReminderDTO>(result as ReminderDTO, notNullValue())
        assertThat(result.title, `is`(reminder.title))
        assertThat(result.description, `is`(reminder.description))
        assertThat(result.location, `is`(reminder.location))
        assertThat(result.latitude, `is`(reminder.latitude))
        assertThat(result.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveRemindersAndGetAllReminders() = runBlockingTest {
        // GIVEN - save multiple reminders
        database.reminderDao()
            .saveReminder(ReminderDTO("title1", "description1", "location1", 100.0, -100.0))
        database.reminderDao()
            .saveReminder(ReminderDTO("title2", "description2", "location2", 110.0, -110.0))
        database.reminderDao()
            .saveReminder(ReminderDTO("title3", "description3", "location3", 120.0, -120.0))

        // WHEN - get all reminders from database
        val result = database.reminderDao().getReminders()

        // THEN - retrieved list size is 3
        assertThat(result, notNullValue())
        assertThat(result.size, `is`(3))
    }

    @Test
    fun saveRemindersAndDeleteReminders() = runBlockingTest {
        // GIVEN - save multiple reminders
        database.reminderDao()
            .saveReminder(ReminderDTO("title1", "description1", "location1", 100.0, -100.0))
        database.reminderDao()
            .saveReminder(ReminderDTO("title2", "description2", "location2", 110.0, -110.0))

        // WHEN - delete all reminders from database
        database.reminderDao().deleteAllReminders()

        // THEN - retrieved list is empty
        val result = database.reminderDao().getReminders()
        assertThat(result, `is`(emptyList()))
    }

}