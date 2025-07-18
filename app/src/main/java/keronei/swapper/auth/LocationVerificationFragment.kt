package keronei.swapper.auth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import com.vmadalin.easypermissions.EasyPermissions
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import keronei.swapper.R
import keronei.swapper.dashboard.DashboardViewModel
import keronei.swapper.databinding.FragmentLocationVerificationBinding
import keronei.swapper.utils.PermissionCallback
import keronei.swapper.utils.getPhotosDir
import keronei.swapper.utils.hasGpsSensor
import java.io.File

@AndroidEntryPoint
class LocationVerificationFragment : Fragment(), LocationListenable, PermissionCallback {

    private var obtainedLocation: Location? = null

    private val dashViewModel: DashboardViewModel by activityViewModels()

    private var _binding: FragmentLocationVerificationBinding? = null

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bleScanner: BluetoothLeScanner? = null
    private var scanSettings: ScanSettings? = null

    private val binding get() = _binding!!

    private var previewRunning = false

    lateinit var imageCapture: ImageCapture
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    lateinit var cameraProvider: ProcessCameraProvider
    lateinit var preview: Preview

    private var capturedPhotoName: String? = null

    override fun onObtained(location: Location) {
        onLocationObtained(location)
    }

    override fun onInAccurate(location: Location) {}

    private fun onLocationObtained(location: Location) {
        obtainedLocation = location
        //binding.btnContinue.isEnabled = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationVerificationBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkGPSFeatureAvailability()

        val limit = 100f
        binding.accuracyView.registerCallback(limit, this)

        val bleAvailable = checkBLEFeatureAvailability()

        if (bleAvailable) {
            startBLE()

            toggleUIOnBTStatus()
        }

        binding.enableBt.setOnClickListener {
            val hasConnectPermission = hasConnectPerm()
            if (hasConnectPermission) {
                enableBTInSettings()
            }
        }

        displayImage(R.drawable.placeholder_image)

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        requireContext().registerReceiver(bluetoothReceiver, filter)

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {

        }, ContextCompat.getMainExecutor(requireContext()))

        imageCapture = ImageCapture.Builder()
            .setTargetRotation(view.display.rotation)
            .build()

        binding.launchCamera.setOnClickListener {
            if (previewRunning) {
                onOpenCamera()
            } else {
                startPreview()
            }
        }

        binding.checkIn.setOnClickListener {
            findNavController().popBackStack()
            dashViewModel.loggedIn = true
        }

        guidesOnPrompt()
    }

    private fun guidesOnPrompt() {
        binding.gpsGuide.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext()).setMessage(getString(R.string.location_guide)).setPositiveButton("Dismiss") { _, _ ->

            }.show()
        }

        binding.selfieGuide.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext()).setMessage(getString(R.string.selfie_guide)).setPositiveButton("Dismiss") { _, _ ->

            }.show()

        }

        binding.stationSignalGuide.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext()).setMessage(getString(R.string.station_signal_guide)).setPositiveButton("Dismiss") { _, _ -> }.show()

        }
    }

    private fun startPreview() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            EasyPermissions.requestPermissions(
                context as FragmentActivity,
                "This app needs access to the camera to take pictures.",
                PERMISSION_REQUEST_CODE_CAMERA,
                Manifest.permission.CAMERA,
            )
            return
        }

        binding.previewView.visibility = View.VISIBLE
        binding.selfieDisplay.visibility = View.GONE

        capturedPhotoName?.let { previusPhoto ->
            val photosDir = getPhotosDir(requireContext())

            val photoFile = File(photosDir, previusPhoto)

            if (photoFile.exists()) {
                val deleted = photoFile.delete()
                if (deleted) {
                    Log.d("CameraX", "Photo deleted successfully.")
                    capturedPhotoName = null
                } else {
                    Log.e("CameraX", "Failed to delete photo.")
                }
            } else {
                Log.w("CameraX", "Photo file not found.")
            }
        }

        cameraProvider = cameraProviderFuture.get()

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        preview = Preview.Builder()
            .build()
            .also {
                it.surfaceProvider = binding.previewView.surfaceProvider
            }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview)
            binding.launchCamera.text = "Take Photo"
            previewRunning = true

        } catch (exc: Exception) {
            Log.e("CameraX", "Use case binding failed", exc)
        }
    }

    private fun displayImage(uri: Any) {
        Glide.with(this)
            .load(uri)
            .centerCrop() // Apply centerCrop transformation
            .circleCrop() // Apply circular transformation after centerCrop
            .placeholder(R.drawable.ic_launcher_background) // Optional: placeholder while loading
            .error(R.drawable.ic_launcher_foreground) // Optional: image to display on error
            .into(binding.selfieDisplay)
    }

    private fun onOpenCamera() {

        binding.previewView.visibility = View.VISIBLE

        val photosDir = getPhotosDir(requireContext())

        val photoName = "IMG_${System.currentTimeMillis()}.jpg"

        val photoFile = File(photosDir, photoName)

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        if (!::imageCapture.isInitialized) {
            Log.e("CameraX", "imageCapture not initialized")
            return
        }

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException) {
                    // insert your code here.
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    binding.previewView.visibility = View.GONE
                    binding.selfieDisplay.visibility = View.VISIBLE

                    outputFileResults.savedUri?.let { taken ->
                        capturedPhotoName = photoName
                        displayImage(taken)
                    }

                    binding.launchCamera.text = "Retake"
                    binding.checkIn.isEnabled = true
                    stopCameraPreview()

                    Log.i("CameraX", "Image saved to ${outputFileResults.savedUri}")
                    // insert your code here.
                }
            })
    }

    private fun stopCameraPreview() {
        if (::cameraProviderFuture.isInitialized) {
            cameraProvider.unbind(preview) // or unbindAll() if needed
            Log.d("CameraX", "Preview stopped")
            previewRunning = false
        }
    }

    private fun enableBTInSettings() {
        val enableBluetooth =
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(
            enableBluetooth,
            REQUEST_ENABLE_BLUETOOTH
        )
    }

    private fun hasConnectPerm(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasPerm = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) {
                return true
            } else {
                requestConnectPermission()
                return false
            }
        } else {
            return true
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestConnectPermission() {
        val broughtContext = context as? FragmentActivity
        if (broughtContext != null) {
            EasyPermissions.requestPermissions(
                context as FragmentActivity,
                "This app needs access to Bluetooth in order to verify your station.",
                BT_PERMISSION_REQUEST_CODE_CONNECT,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else {
            Log.e("VerificationFragment", "The received context is not of FragmentActivity, cannot request permission. ${requireContext()::class.java.name}")
        }
    }

    private fun toggleUIOnBTStatus() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            binding.bleSignalStrength.visibility = View.GONE
            binding.enableBt.visibility = View.VISIBLE
            binding.signalPercentageTextView.visibility = View.GONE
        } else {
            binding.bleSignalStrength.visibility = View.VISIBLE
            binding.enableBt.visibility = View.GONE
            binding.signalPercentageTextView.visibility = View.VISIBLE

            val hasBTPermission = checkBTPermission()
            Log.i("BLEScanner", "Has permission: $hasBTPermission")

            if (hasBTPermission) {
                startScan()
            } else {
                requestPermission()
            }
        }
    }

    private fun requestPermission() {
        val broughtContext = context as? FragmentActivity
        if (broughtContext != null) {
            EasyPermissions.requestPermissions(
                context as FragmentActivity,
                "This app needs access to Bluetooth in order to verify your station.",
                BT_PERMISSION_REQUEST_CODE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
            )
        } else {
            Log.e("VerificationFragment", "The received context is not of FragmentActivity, cannot request permission. ${requireContext()::class.java.name}")
        }
    }

    private fun startScan() {
        bleScanner = bluetoothAdapter?.bluetoothLeScanner

        scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

        val scanFilters = listOf(
            ScanFilter.Builder()
                .build()
        )

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestScanPermission()
            Log.i("BLEScanner", "Scan permission denied, requesting")
            return
        }

        Log.i("BLEScanner", "Going to start scan")
        bleScanner?.startScan(scanFilters, scanSettings, bleScanCallback)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestScanPermission() {
        val broughtContext = context as? FragmentActivity
        if (broughtContext != null) {
            EasyPermissions.requestPermissions(
                context as FragmentActivity,
                "This app needs access to Bluetooth in order to verify your station.",
                BT_SCAN_PERMISSION_REQUEST_CODE,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            Log.e("VerificationFragment", "The received context is not of FragmentActivity, cannot request permission. ${requireContext()::class.java.name}")
        }
    }

    private val bleScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult?) {
            // Scan result received.
            Log.d("Found Devices", result?.device?.address ?: result?.rssi.toString())

            updateSignalDisplay(result?.rssi ?: 0)
        }

        override fun onScanFailed(errorCode: Int) {
            // OOPS, there is something wrong.
            Log.e("BLEScan", "Failed to scan with error code: $errorCode")
        }
    }

    private fun checkBTPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
        )
    }

    private fun checkGPSFeatureAvailability() {
        activity?.hasGpsSensor(activity as FragmentActivity) { available ->
            if (!available) {
                Snackbar.make(binding.root, "Device does not have GPS.", Snackbar.LENGTH_LONG).show()
                return@hasGpsSensor
            }
        }
    }

    private fun updateSignalDisplay(rssiValue: Int) {
        binding.bleSignalStrength.rssi = rssiValue // Update the custom view

        // Calculate and display percentage
        val percentage = binding.bleSignalStrength.getSignalPercentage(rssiValue)
        binding.signalPercentageTextView.text = "Signal Percentage: ${String.format("%.1f", percentage)}%"
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                            toggleUIOnBTStatus()
                            Log.d("BT", "Bluetooth is OFF")
                        }

                        BluetoothAdapter.STATE_TURNING_OFF -> Log.d("BT", "Turning Bluetooth OFF")
                        BluetoothAdapter.STATE_ON -> {
                            toggleUIOnBTStatus()
                            Log.d("BT", "Bluetooth is ON")
                        }

                        BluetoothAdapter.STATE_TURNING_ON -> Log.d("BT", "Turning Bluetooth ON")
                    }
                }
            }
        }
    }

    private fun checkBLEFeatureAvailability(): Boolean {
        return requireContext().packageManager
            .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

    }

    private fun startBLE() {
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        if (bluetoothManager != null) bluetoothAdapter = bluetoothManager.adapter
    }

    override fun onDetach() {
        super.onDetach()

        requireContext().unregisterReceiver(bluetoothReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onGranted(requestCode: Int, perms: List<String>) {
        when (requestCode) {
            BT_PERMISSION_REQUEST_CODE -> {
                startScan()
            }

            BT_PERMISSION_REQUEST_CODE_CONNECT -> {
                enableBTInSettings()
            }

            PERMISSION_REQUEST_CODE_CAMERA -> {
                onOpenCamera()
            }

            else -> {
                Toast.makeText(context, "Permission granted.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDenied(requestCode: Int, perms: List<String>) {
        Toast.makeText(context, "Permission denied.", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val REQUEST_ENABLE_BLUETOOTH = 100
        const val BT_PERMISSION_REQUEST_CODE = 101
        const val BT_SCAN_PERMISSION_REQUEST_CODE = 102
        const val BT_PERMISSION_REQUEST_CODE_CONNECT = 103
        const val PERMISSION_REQUEST_CODE_CAMERA = 104
    }
}