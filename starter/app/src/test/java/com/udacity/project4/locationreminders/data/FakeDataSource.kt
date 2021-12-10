package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    //    private var data: MutableList<ReminderDTO> = mutableListOf()
    private var data: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()
    var shouldReturnError: Boolean = false

//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return when (shouldReturnError) {
            true -> Result.Error(ERROR_MESSAGE, ERROR_STATUS_CODE)
            else -> {
                Result.Success(data.values.toList())
            }
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        data[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return Result.Success(data[id] as ReminderDTO)

    }

    override suspend fun deleteAllReminders() {
        TODO("delete all the reminders")
    }

    fun addReminders(vararg reminders: ReminderDTO) {
        for (reminder in reminders) {
            data[reminder.id] = reminder
        }
    }
}

const val ERROR_MESSAGE = "ERROR"
const val ERROR_STATUS_CODE = -1