package keronei.swapper.auth

import android.location.Location
import android.util.Log
import androidx.core.location.LocationListenerCompat

class CustomLocationListener(
    val onLocationChangedCallback: (location: Location) -> Unit
) : LocationListenerCompat {
    override fun onLocationChanged(location: Location) {
        onLocationChangedCallback(location)
    }

    override fun onProviderDisabled(provider: String) {
        Log.e("CustomLocationListener", "$provider was disabled")
    }

    override fun onLocationChanged(locations: MutableList<Location>) {
        locations.minByOrNull { it.accuracy }?.let { location ->
            onLocationChangedCallback(location)
        }
    }

    override fun onProviderEnabled(provider: String) {
        Log.d("CustomLocationListener", "$provider was enabled")
    }

    override fun onFlushComplete(requestCode: Int) {
        Log.d("CustomLocationListener", "$requestCode flash was complete")
    }
}
