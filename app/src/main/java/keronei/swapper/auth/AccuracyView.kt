package keronei.swapper.auth

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle.State
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vmadalin.easypermissions.EasyPermissions
import dagger.hilt.android.EntryPointAccessors
import keronei.swapper.R
import keronei.swapper.databinding.ViewAccuracyBinding
import keronei.swapper.di.AppDependencies
import keronei.swapper.utils.PermissionCallback
import keronei.swapper.utils.PermissionListenable
import keronei.swapper.utils.collectOnResumed
import keronei.swapper.utils.customViewLifeCycleOwner
import keronei.swapper.utils.hasGPSSensor
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccuracyView @JvmOverloads constructor(
    private val context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes),
    LocationControllable,
    PermissionCallback {

    private var binding: ViewAccuracyBinding
    private var collectLocationJob: Job? = null
    private var listener: LocationListenable? = null
    private var accuracy: Float? = null
    private val lifecycleOwner by customViewLifeCycleOwner()

    private val locationUtil: LocationUtil by lazy {
        val appContext = context.applicationContext
        EntryPointAccessors.fromApplication(appContext, AppDependencies::class.java).locationUtil()
    }

    private val updatePolicyState = MutableStateFlow<UpdatePolicy>(UpdatePolicy.StopWhenAccurate)

    init {
        val inflater = LayoutInflater.from(context)
        binding = ViewAccuracyBinding.inflate(inflater, this, true)
        observeLifeCycle()
    }

    //region LocationControllable
    override fun registerCallback(accuracyLimit: Float?, callback: LocationListenable) {
        this.listener = callback
        this.accuracy = accuracyLimit
    }

    override fun setUpdatePolicy(policy: UpdatePolicy) {
        updatePolicyState.update { policy }
    }

    override fun restart() {
        //TODO: Reset view to original state
        observeLocationData()
    }
    //endregion

    //region Observe data
    private fun observeFlow() {
        observeLocationData()
        observeUpdatePolicy()
    }

    private fun observeLifeCycle() {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.lifecycle.repeatOnLifecycle(State.RESUMED) {
                checkGpsAvailable()
            }
        }

        if (context is PermissionListenable) {
            context.registerCallback(lifecycleOwner, this)
        }
    }

    override fun onGranted(requestCode: Int, perms: List<String>) {
        if (requestCode != PERMISSION_REQUEST_CODE) return
        if (context.hasLocationPermission()) {
            binding.tvError.isVisible = false
            observeFlow()
        }
    }

    override fun onDenied(requestCode: Int, perms: List<String>) {
        if (requestCode != PERMISSION_REQUEST_CODE) return
        if (!context.hasLocationPermission()) {
            binding.tvError.isVisible = true
            binding.tvError.setText(R.string.pls_grant_location_permission)
        }
    }

    private fun observeUpdatePolicy() {
        updatePolicyState.collectOnResumed(lifecycleOwner) { policy ->
            if (policy == UpdatePolicy.NeverStop && collectLocationJob?.isActive != true) {
                observeLocationData()
            }
        }
    }

    private fun observeLocationData() {
        collectLocationJob?.cancel()
        collectLocationJob = locationUtil.getLocationFlow()
            .onStart {
                binding.tvAccuracy.text = "--"
                updateSignColor(AccuracyState.InAccurate)
            }
            .onStart { listener?.onStartListening() }
            .onEach(::onLocationUpdated)
            .map(::getAccuracyState)
            .combine(updatePolicyState) { state, policy ->
                if (state is AccuracyState.Obtained && policy is UpdatePolicy.StopWhenAccurate) {
                    collectLocationJob?.cancel()
                }
            }
            .collectOnResumed(lifecycleOwner)
    }
    //endregion

    //region Check permission
    private fun checkGpsAvailable() {
        if (!context.hasGPSSensor()) { //Ensure GPS available
            binding.tvError.setText(R.string.gps_sensor_is_unavailable)
            return
        }

        if (context.hasLocationPermission()) { //Start to listen location if has permission
            observeFlow()
        } else {
            requestPermission()
        }
    }


    private fun requestPermission() {
        val broughtContext = context as? FragmentActivity
        if (broughtContext != null) {
            EasyPermissions.requestPermissions(
                context as FragmentActivity,
                "This app needs access to your location to function properly",
                PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        } else {
            Log.e("AccuracyView", "The received context is not of FragmentActivity, cannot request permission. ${context::class.java.name}")
        }
    }

    //endregion

    //region Update UI
    private fun onLocationUpdated(location: Location) {
        binding.tvAccuracy.text = location.accuracy.toInt().toString()
        listener?.onLocationCollected(location)

        val state = getAccuracyState(location)
        updateSignColor(state)
        updateLayout(state)

        when (state) {
            AccuracyState.Obtained -> listener?.onObtained(location)
            AccuracyState.InAccurate -> listener?.onInAccurate(location)
        }
    }

    private fun updateLayout(state: AccuracyState) {
        when (state) {
            AccuracyState.Obtained -> {
                binding.tipsLayout.isVisible = false
                binding.informLayout.isVisible = true
            }

            AccuracyState.InAccurate -> {
                binding.tipsLayout.isVisible = true
                binding.informLayout.isVisible = false
            }
        }
    }

    private fun updateSignColor(state: AccuracyState) {
        val colorStateList = ContextCompat.getColorStateList(context, state.colorRes)
        binding.viewBackground.backgroundTintList = colorStateList
        binding.viewBackgroundLighter.backgroundTintList = colorStateList
    }

    private fun getAccuracyState(location: Location): AccuracyState {
        val accuracyLimit = accuracy ?: locationUtil.accuracyLimit
        return if (location.hasAccuracy() && location.accuracy <= accuracyLimit) {
            AccuracyState.Obtained
        } else {
            AccuracyState.InAccurate
        }
    }
    //endregion

    sealed class AccuracyState(@ColorRes val colorRes: Int) {
        object InAccurate : AccuracyState(R.color.grey4)
        object Obtained : AccuracyState(R.color.teal_200)
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 2023
        const val REQUEST_CHECK_SETTINGS = 200

    }
}

interface LocationListenable {
    fun onStartListening() {}
    fun onObtained(location: Location) {}
    fun onInAccurate(location: Location) {}
    fun onLocationCollected(location: Location) {}
}

sealed interface UpdatePolicy {
    /**
     * This option will let the view collect continuously the location.
     */
    object NeverStop : UpdatePolicy

    /**
     * This option will let the view know stop once the location has the accuracy smaller than 10m
     */
    object StopWhenAccurate : UpdatePolicy
}

interface LocationControllable {
    fun restart()

    fun registerCallback(accuracyLimit: Float?, callback: LocationListenable)

    fun setUpdatePolicy(policy: UpdatePolicy)
}

fun Context.hasLocationPermission(): Boolean {
    return EasyPermissions.hasPermissions(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
}
