package com.hypertrack.android.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hypertrack.android.repository.DriverRepo
import com.hypertrack.android.utils.Destination
import com.hypertrack.android.utils.HyperTrackService
import kotlinx.coroutines.launch

class LoginViewModel(
    private val driverRepo: DriverRepo,
    private val hyperTrackService: HyperTrackService
) : ViewModel() {

    private val _checkInButtonEnabled = MutableLiveData(false)

    private val _destination = MutableLiveData(Destination.LOGIN)

    private val _showProgress = MutableLiveData(false)
    val showProgresss
        get() = _showProgress

    val enableCheckIn: LiveData<Boolean>
        get() = _checkInButtonEnabled

    val destination : LiveData<Destination>
        get() = _destination


    fun onTextChanged(input: CharSequence) {
        when {
            input.isNotEmpty() -> _checkInButtonEnabled.postValue(true)
            else -> _checkInButtonEnabled.postValue(false)
        }
    }

    fun onLoginClick(inputText: CharSequence?) {
        inputText?.let {
            _checkInButtonEnabled.postValue(false)
            _showProgress.postValue(true)
            val driverId = it.toString()
            Log.d(TAG, "Proceeding with Driver Id $driverId")
            hyperTrackService.driverId = driverId
            driverRepo.driverId = driverId
            viewModelScope.launch {
                _destination.postValue(Destination.PERMISSION_REQUEST)
                _showProgress.postValue(false)
            }
            return
        }

    }

    fun checkAutoLogin() {
        Log.v(TAG, "checkAutoLogin")
        if (driverRepo.hasDriverId) {
            onLoginClick(driverRepo.driverId)
        }
    }

    companion object {
        const val TAG = "LoginVM"
    }
}