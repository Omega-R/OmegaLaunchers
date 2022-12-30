package com.omegar.libs.omegalaunchers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.omegar.libs.omegalaunchers.tools.BundlePair
import com.omegar.libs.omegalaunchers.tools.bundleOf
import com.omegar.libs.omegalaunchers.tools.equalsBundle
import com.omegar.libs.omegalaunchers.tools.hashCodeBundle
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anton Knyazev on 06.04.2019.
 */
@Parcelize
open class ActivityLauncher(
    protected val activityClass: Class<out Activity>,
    private val bundle: Bundle? = null,
    private var flags: Int = 0
) : BaseIntentLauncher() {

    companion object {

        fun launch(context: Context, option: Bundle? = null, vararg launchers: ActivityLauncher) {
            launchers
                .lastOrNull()
                ?.launch(context, option, *launchers.take(launchers.size - 1).toTypedArray())
        }

    }

    constructor(activityClass: Class<out Activity>, vararg extraParams: BundlePair, flags: Int = 0)
            : this(activityClass, bundleOf(*extraParams), flags)

    override fun getIntent(context: Context): Intent {
        return Intent(context, activityClass).apply {
            if (bundle != null) {
                putExtras(bundle)
            }
            flags = this@ActivityLauncher.flags
        }
    }

    fun addFlags(flag: Int) = apply {
        flags = flags or flag
    }

    fun removeFlags(flag: Int) = apply {
        flags = flags and (flag.inv())
    }

    fun clearTaskOnLaunch() = apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    fun isOurActivity(activity: Activity): Boolean {
        return activityClass.isInstance(activity)
                && activity.intent.extras.equalsBundle(bundle)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActivityLauncher

        if (activityClass != other.activityClass) return false
        if (!bundle.equalsBundle(other.bundle)) return false
        if (flags != other.flags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = activityClass.hashCode()
        result = 31 * result + (bundle?.hashCodeBundle() ?: 0)
        result = 31 * result + flags
        return result
    }

    interface DefaultCompanion {

        fun createLauncher(): ActivityLauncher

        fun launch(context: Context, option: Bundle? = null) {
            createLauncher()
                .launch(context, option)
        }

    }

}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> T.createActivityLauncher(
    vararg extra: BundlePair,
    flags: Int = 0
): ActivityLauncher {
    val declaringClass = T::class.java.declaringClass
    return ActivityLauncher(declaringClass as Class<out Activity>, *extra, flags = flags)
}


@Suppress("UNCHECKED_CAST")
inline fun <reified T> T.createActivityLauncher(flags: Int = 0): ActivityLauncher {
    val declaringClass = T::class.java.declaringClass
    return ActivityLauncher(declaringClass as Class<out Activity>, flags = flags)
}
