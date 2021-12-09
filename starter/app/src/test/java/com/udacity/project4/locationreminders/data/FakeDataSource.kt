package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    private var data: MutableList<ReminderDTO> = mutableListOf()
    var isResultSuccess: Boolean = false

//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return when (isResultSuccess) {
            true -> Result.Success(data)
            else -> {
                Result.Error(ERROR_MESSAGE, ERROR_STATUS_CODE)
            }
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        TODO("save the reminder")
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        TODO("return the reminder with the id")
    }

    override suspend fun deleteAllReminders() {
        TODO("delete all the reminders")
    }

    fun addReminders(vararg reminders: ReminderDTO) {
        for (reminder in reminders) {
            data.add(reminder)
        }
    }


}

const val ERROR_MESSAGE = "ERROR"
const val ERROR_STATUS_CODE = -1