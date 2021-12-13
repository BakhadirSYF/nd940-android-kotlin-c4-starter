package com.udacity.project4.authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.udacity.project4.FirebaseUserLiveData

class AuthenticationActivityViewModel : ViewModel() {
    companion object {
        const val TAG = "AuthActivityViewModel"
    }

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        Log.d(TAG, "authenticationState")
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

}