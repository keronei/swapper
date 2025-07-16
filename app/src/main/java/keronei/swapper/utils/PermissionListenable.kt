package keronei.swapper.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

interface PermissionListenable {
    fun registerCallback(lifecycleOwner: LifecycleOwner, callback: PermissionCallback)
}

interface PermissionChangeNotifier {
    fun notifyPermissionGranted(requestCode: Int, perms: List<String>)

    fun notifyPermissionDenied(requestCode: Int, perms: List<String>)
}

interface PermissionProvider : PermissionListenable, PermissionChangeNotifier

interface PermissionCallback {
    fun onGranted(requestCode: Int, perms: List<String>)

    fun onDenied(requestCode: Int, perms: List<String>)
}

class PermissionListenHandler : PermissionProvider {
    private val permissionCallbacks = mutableSetOf<PermissionCallback>()

    override fun registerCallback(lifecycleOwner: LifecycleOwner, callback: PermissionCallback) {
        //In case the current state of observer is already on RESUMED and not update, the lifecycle
        //observer will not be triggered. So we will check and add immediately
        val shouldAddImmediate =
            lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
        if (shouldAddImmediate) {
            permissionCallbacks.add(callback)
        }

        //Observe to add and remove the callback automatically.
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
                permissionCallbacks.add(callback)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                permissionCallbacks.remove(callback)
            }
        })
    }

    override fun notifyPermissionGranted(requestCode: Int, perms: List<String>) {
        permissionCallbacks.forEach {
            it.onGranted(requestCode, perms)
        }
    }

    override fun notifyPermissionDenied(requestCode: Int, perms: List<String>) {
        permissionCallbacks.forEach {
            it.onDenied(requestCode, perms)
        }
    }
}
