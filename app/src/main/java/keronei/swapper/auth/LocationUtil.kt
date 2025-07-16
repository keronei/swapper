package keronei.swapper.auth

import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices.*
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


sealed class LocationEvent(val eventName: String, val param: String) {
    object GetFailure : LocationEvent("get_location_failure", "error")
    object CanNotFoundProvider : LocationEvent("can_not_found_provider", "details")
}

interface LocationUtil {
    suspend fun getCurrentLocationNonNull(): Location

    suspend fun getCurrentLocation(): Location?

    fun getLocationFlow(): Flow<Location>

    val accuracyLimit: Float
}

class LocationProvider(private val context: Context) : LocationUtil {

    @OptIn(DelicateCoroutinesApi::class)
    private val locationFlow = buildLocationFlow()
        .flowOn(Dispatchers.Main)
        .catch {
            logEvent(LocationEvent.GetFailure, it.toString())
        }.shareIn(
            replay = 1,
            scope = GlobalScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = 1000L,
                replayExpirationMillis = 1000L,
            ),
        )

    override suspend fun getCurrentLocationNonNull(): Location {
        return suspendCancellableCoroutine { continuation ->
            runBlocking {
                val location = getLocationFlow().filterNotNull().first()
                continuation.resume(location)
            }
        }
    }

    override suspend fun getCurrentLocation() = getLocationFlow().firstOrNull()

    override fun getLocationFlow() = locationFlow

    override val accuracyLimit get() = FUSED_LOCATION_ACCURACY_LIMIT

    /**
     * The GPS provider provide higher accuracy. But some time it not available or take too long to take location.
     * Ideally, we will collect location from both of GPS and FusedLocation.
     * Once both 2 provider has data, we will combine them to select the location has smaller accuracy, then cancel
     * 2 single collectors.
     */
    private fun buildLocationFlow(): Flow<Location> {
        return channelFlow {
            val bestLocationFlow = getHighBestLocationFlow()
                .shareIn(this, SharingStarted.WhileSubscribed(1000), 1)

            val fusedLocationFlow = getFusedLocationFlow()
                .shareIn(this, SharingStarted.WhileSubscribed(1000), 1)

            val collectFusedLocationJob = fusedLocationFlow.onEach(::send)
                .onEach {
                    Log.d("---Location Fused: ", "Accuracy: ${it.accuracy} - $it")
                }
                .launchIn(this)

            val collectBestLocationJob = bestLocationFlow.onEach(::send)
                .onEach {
                    Log.d("---Location Best: ", "Accuracy: ${it.accuracy} - $it")
                }
                .launchIn(this)

            combine(bestLocationFlow, fusedLocationFlow, ::selectBetterLocation)
                .onEach(::send)
                .onEach {
                    collectBestLocationJob.apply { if (isActive) cancel() }
                    collectFusedLocationJob.apply { if (isActive) cancel() }
                }
                .launchIn(this)
        }
    }

    private fun selectBetterLocation(location1: Location, location2: Location): Location {
        return if (location1.accuracy < location2.accuracy) location1
        else location2
    }

    private fun getHighBestLocationFlow(): Flow<Location> {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria().apply {
            isCostAllowed = true
            isSpeedRequired = true
            isBearingRequired = true
            isAltitudeRequired = true
            speedAccuracy = Criteria.ACCURACY_HIGH
            powerRequirement = Criteria.POWER_HIGH
            bearingAccuracy = Criteria.ACCURACY_HIGH
            verticalAccuracy = Criteria.ACCURACY_HIGH
            horizontalAccuracy = Criteria.ACCURACY_HIGH
        }

        val provider = locationManager.getBestProvider(criteria, true) ?: run {
            Log.d("Location","Location: Can not found provider")
            return emptyFlow()
        }

        return callbackFlow {
            val listener = CustomLocationListener { location->
                trySendBlocking(location)
            }

            locationManager.requestLocationUpdates(
                provider,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                listener,
            )

            awaitClose {
                locationManager.removeUpdates(listener)
            }
        }
    }

    private fun getFusedLocationFlow(): Flow<Location> {
        val looper: Looper = Looper.getMainLooper()
        val fusedLocationClient = getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, MIN_TIME_BW_UPDATES)
            .setMinUpdateIntervalMillis(MIN_TIME_BW_UPDATES).setWaitForAccurateLocation(true)
            .build()

        return callbackFlow {
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    trySendBlocking(result.locations.minBy { it.accuracy })
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                looper,
            )

            awaitClose {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    private fun logEvent(event: LocationEvent, payload: String) {

    }

    companion object {
        private const val MIN_TIME_BW_UPDATES = 500L
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 1f
        const val FUSED_LOCATION_ACCURACY_LIMIT = 11.0F
    }
}
