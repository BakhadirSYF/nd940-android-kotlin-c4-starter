package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.example.android.architecture.blueprints.todoapp.util.wrapEspressoIdlingResource
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {
            Log.d(TAG, "enqueueWork")

            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        Log.d(TAG, "onHandleWork")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            val errorMessage =
                errorMessage(baseContext.applicationContext, geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }

        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.v(TAG, baseContext.applicationContext.getString(R.string.geofence_entered))
            if (geofencingEvent.triggeringGeofences.isNotEmpty()) {
                for (geofence in geofencingEvent.triggeringGeofences) {
                    sendNotification(geofence.requestId)
                }
            } else {
                Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                return
            }
        }

    }

    //TODO: get the request id of the current geofence
    private fun sendNotification(requestId: String) {
        Log.d(TAG, "sendNotification, requestId = $requestId")
        // Get the local repository instance
        val remindersLocalRepository: ReminderDataSource by inject()
        // Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = wrapEspressoIdlingResource {
                remindersLocalRepository.getReminder(requestId)
            }
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                //send a notification to the user with the reminder details
                sendNotification(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }
}

private const val TAG = "GeofenceJobIntentServ"