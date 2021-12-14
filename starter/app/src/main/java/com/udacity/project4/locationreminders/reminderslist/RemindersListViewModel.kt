package com.udacity.project4.locationreminders.reminderslist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.android.architecture.blueprints.todoapp.util.wrapEspressoIdlingResource
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result.Success
import com.udacity.project4.locationreminders.data.dto.Result.Error
import kotlinx.coroutines.launch

class RemindersListViewModel(private val dataSource: ReminderDataSource) : BaseViewModel() {
    // list that holds the reminder data to be displayed on the UI
    val remindersList = MutableLiveData<List<ReminderDataItem>>()

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {
        showLoading.value = true
        viewModelScope.launch {
            // Interacting with the dataSource has to be through a coroutine
            val result = wrapEspressoIdlingResource {
                dataSource.getReminders()
            }
            showLoading.postValue(false)
            when (result) {
                is Success<*> -> {
                    val dataList = ArrayList<ReminderDataItem>()
                    dataList.addAll((result.data as List<ReminderDTO>).map { reminder ->
                        //map the reminder data from the DB to the be ready to be displayed on the UI
                        ReminderDataItem(
                            reminder.title,
                            reminder.description,
                            reminder.location,
                            reminder.latitude,
                            reminder.longitude,
                            reminder.id
                        )
                    })
                    remindersList.value = dataList
                }
                is Error ->
                    showSnackBar.value = result.message
            }

            //check if no data has to be shown
            invalidateShowNoData()
        }
    }

    /**
     * Inform the user that there's no data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value == null || remindersList.value!!.isEmpty()
    }
}