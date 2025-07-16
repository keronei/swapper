package keronei.swapper.utils

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import keronei.swapper.auth.AccuracyView.Companion.REQUEST_CHECK_SETTINGS
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

fun <T> Flow<T>.collectOnResumed(
    lifecycleOwner: LifecycleOwner, callback: suspend (T) -> Unit = {}
): Job {
    return collectOnState(lifecycleOwner, State.RESUMED, callback)
}

fun <T> Flow<T>.collectOnState(
    lifecycleOwner: LifecycleOwner, state: State, callback: suspend (T) -> Unit = {}
): Job {
    return lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.lifecycle.repeatOnLifecycle(state) {
            collectLatest { callback(it) }
        }
    }
}

fun changeLocationSettings(
    activity: Activity,
    requestCode: Int,
    getLocationUpdates: (locationRequest: LocationRequest) -> Unit
) {
    val locationRequest = LocationRequest
        .Builder(PRIORITY_HIGH_ACCURACY, 1000)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(1000)
        .build()

    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    val client: SettingsClient = LocationServices.getSettingsClient(activity)
    val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
    task.addOnSuccessListener {
        getLocationUpdates(locationRequest)
    }.addOnFailureListener { exception ->
        if (exception is ApiException) {
            when (exception.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    try {
                        exception.status.startResolutionForResult(
                            activity,
                            requestCode
                        )
                        val result = exception.status.resolution
                        Log.d("LOCATION_SETTINGS", "result => ${result?.intentSender}")

                    } catch (sendEx: IntentSender.SendIntentException) {
                        Log.e("Location", sendEx.localizedMessage)
                    }
                }

                else -> {
                    Log.d("LOCATION_SETTINGS", "STATUS => ${exception.status}")
                }
            }
        } else {
            Log.d("LOCATION_SETTINGS", "LocationSettingException => $exception")
        }
    }
}

fun Context.hasGPSSensor(): Boolean {
    val packageManager = packageManager
    val featureGPS = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
    Log.d("Permission", "GPS: $featureGPS")
    return featureGPS
}

fun isMockLocationEnabled(context: Context): Boolean {
    var isMockLocation = false

    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        for (provider in locationManager.allProviders) {
            val location = locationManager.getLastKnownLocation(provider)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (location != null && location.isMock) {
                    isMockLocation = true
                    break
                }
            } else {
                if (location != null && location.isFromMockProvider) {
                    isMockLocation = true
                    break
                }
            }
        }
    } catch (exception: Exception) {
        exception.printStackTrace()
    }

    return isMockLocation
}

fun Context.hasGpsSensor(
    activity: Activity,
    gpsAvailability: (available: Boolean) -> Unit
) {
    val hasGpsSensors = hasGPSSensor()
    if (!hasGpsSensors) {
        gpsAvailability(false)
    } else {
        val isFromMock = isMockLocationEnabled(activity)
        Log.d("MockDetect", "IsMock => $isFromMock")
        changeLocationSettings(activity, REQUEST_CHECK_SETTINGS) {
            gpsAvailability(true)
        }
    }
}

fun getPhotosDir(context: Context): File {
    val photoDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "verification-photos")
    if (!photoDir.exists()) {
        photoDir.mkdirs() // create if not exists
    }

    return photoDir

}