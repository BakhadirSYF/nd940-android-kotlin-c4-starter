package com.udacity.project4.authentication

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
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

}