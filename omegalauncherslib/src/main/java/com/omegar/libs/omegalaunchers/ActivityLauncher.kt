package com.omegar.libs.omegalaunchers

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AndroidRuntimeException
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.omegar.libs.omegalaunchers.tools.BundlePair
import com.omegar.libs.omegalaunchers.tools.bundleOf
import com.omegar.libs.omegalaunchers.tools.equalsBundle
import com.omegar.libs.omegalaunchers.tools.hashCodeBundle
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anton Knyazev on 06.04.2019.
 */
@Parcelize
class ActivityLauncher(
    private val activityClass: Class<Activity>,
    private val bundle: Bundle? = null,
    private var flags: Int = 0
) : Launcher, Parcelable {

    companion object {

        fun launch(context: Context, option: Bundle? = null, vararg launchers: ActivityLauncher) {
            launchers
                .lastOrNull()
                ?.launch(context, option, *launchers.take(launchers.size - 1).toTypedArray())
        }

    }

    constructor(activityClass: Class<Activity>, vararg extraParams: BundlePair, flags: Int = 0)
            : this(activityClass, bundleOf(*extraParams), flags)

    private fun createIntent(context: Context): Intent {
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

    override fun launch(context: Context) {
        launch(context, null)
    }

    fun launch(context: Context, option: Bundle? = null) {
        val intent = createIntent(context)
        try {
            context.compatStartActivity(intent, option)
        } catch (exc: AndroidRuntimeException) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.compatStartActivity(intent, option)
        }
    }

    fun launch(context: Context, vararg parentLaunchers: ActivityLauncher) {
        launch(context, null, *parentLaunchers)
    }

    fun launch(context: Context, option: Bundle? = null, vararg parentLaunchers: ActivityLauncher) {
        val list =
            listOf(*parentLaunchers.map { it.createIntent(context) }.toTypedArray(), createIntent(context))

        ContextCompat.startActivities(context, list.toTypedArray(), option)
    }

    private fun Context.compatStartActivity(intent: Intent, option: Bundle? = null) {
        ContextCompat.startActivity(this, intent, option)
    }

    private fun Activity.compatStartActivityForResult(intent: Intent, requestCode: Int, option: Bundle? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            startActivityForResult(intent, requestCode, option)
        } else {
            startActivityForResult(intent, requestCode)
        }
    }

    fun getPendingIntent(
        context: Context, requestCode: Int = 0,
        flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
    ): PendingIntent {
        return PendingIntent.getActivity(context, requestCode, createIntent(context), flags)
    }

    fun getPendingIntent(
        context: Context,
        requestCode: Int = 0,
        flags: Int = PendingIntent.FLAG_UPDATE_CURRENT,
        vararg parentLaunchers: ActivityLauncher

    ): PendingIntent {
        val list =
            listOf(*parentLaunchers.map { it.createIntent(context) }.toTypedArray(), createIntent(context))

        val intents = list.toTypedArray()

        return PendingIntent.getActivities(context, requestCode, intents, flags)
    }

    fun getPendingIntentWithParentStack(
        context: Context,
        requestCode: Int = 0,
        flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
    ): PendingIntent {
        return TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(createIntent(context))
            .getPendingIntent(requestCode, flags)!!
    }

    fun launchForResult(activity: Activity, requestCode: Int, option: Bundle? = null) {
        activity.compatStartActivityForResult(createIntent(activity), requestCode, option)
    }

    fun launchForResult(fragment: Fragment, requestCode: Int, option: Bundle? = null) {
        fragment.startActivityForResult(createIntent(fragment.context!!), requestCode, option)
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
    return ActivityLauncher(declaringClass as Class<Activity>, *extra, flags = flags)
}


@Suppress("UNCHECKED_CAST")
inline fun <reified T> T.createActivityLauncher(flags: Int = 0): ActivityLauncher {
    val declaringClass = T::class.java.declaringClass
    return ActivityLauncher(declaringClass as Class<Activity>, flags = flags)
}
