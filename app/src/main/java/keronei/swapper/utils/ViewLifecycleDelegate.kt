package keronei.swapper.utils

import android.view.View
import android.view.View.OnAttachStateChangeListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlin.reflect.KProperty

fun View.customViewLifeCycleOwner() = ViewLifecycleDelegate(this)

class ViewLifecycleDelegate(view: View) : LifecycleOwner, OnAttachStateChangeListener {

    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        view.addOnAttachStateChangeListener(this)
    }

    override fun onViewAttachedToWindow(v: View) {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onViewDetachedFromWindow(v: View) {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    override val lifecycle: Lifecycle = lifecycleRegistry

    operator fun getValue(accuracyView: View, property: KProperty<*>): LifecycleOwner {
        return this
    }
}
