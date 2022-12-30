package com.omegar.libs.omegalaunchers

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import com.omegar.libs.omegalaunchers.tools.BundlePair
import com.omegar.libs.omegalaunchers.tools.bundleOf
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anton Knyazev on 2019-07-19.
 */
@Parcelize
open class ServiceLauncher(
    protected val serviceClass: Class<out Service>,
    private val bundle: Bundle? = null
) : Launcher {

    constructor(serviceClass: Class<out Service>, vararg extraParams: BundlePair)
            : this(serviceClass, bundleOf(*extraParams))

    protected open fun createIntent(context: Context): Intent {
        return Intent(context, serviceClass).apply {
            bundle?.let(::putExtras)
        }
    }

    override fun launch(context: Context) {
        context.startService(createIntent(context))
    }

    fun launchForeground(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(createIntent(context))
        } else {
            launch(context)
        }
    }

    fun bindService(context: Context, serviceConnection: ServiceConnection, flags: Int = Context.BIND_AUTO_CREATE) {
        context.bindService(createIntent(context), serviceConnection, flags)
    }


}


@Suppress("UNCHECKED_CAST")
inline fun <reified T> T.createServiceLauncher(): ServiceLauncher {
    val declaringClass = T::class.java.declaringClass
    return ServiceLauncher(declaringClass as Class<Service>)
}


@Suppress("UNCHECKED_CAST")
inline fun <reified T> T.createServiceLauncher(
    vararg extra: BundlePair
): ServiceLauncher {
    val declaringClass = T::class.java.declaringClass
    return ServiceLauncher(declaringClass as Class<Service>, *extra)
}

