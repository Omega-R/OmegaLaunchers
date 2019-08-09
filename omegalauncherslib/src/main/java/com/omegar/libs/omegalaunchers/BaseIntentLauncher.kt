package com.omegar.libs.omegalaunchers

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.AndroidRuntimeException
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Created by Anton Knyazev on 2019-07-08.
 */
abstract class BaseIntentLauncher : Launcher {

    protected abstract fun getIntent(context: Context): Intent

    override fun launch(context: Context) {
        launch(context, null)
    }

    fun launch(context: Context, option: Bundle? = null) {
        val intent = getIntent(context)
        try {
            context.compatStartActivity(intent, option)
        } catch (exc: AndroidRuntimeException) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.compatStartActivity(intent, option)
        }
    }

    fun launch(context: Context, vararg parentLaunchers: BaseIntentLauncher) {
        launch(context, null, *parentLaunchers)
    }

    fun launch(context: Context, option: Bundle? = null, vararg parentLaunchers: BaseIntentLauncher) {
        val list =
            listOf(*parentLaunchers.map { it.getIntent(context) }.toTypedArray(), getIntent(context))

        ContextCompat.startActivities(context, list.toTypedArray(), option)
    }

    fun getPendingIntent(
        context: Context, requestCode: Int = 0,
        flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
    ): PendingIntent {
        return PendingIntent.getActivity(context, requestCode, getIntent(context), flags)
    }

    fun getPendingIntent(
        context: Context,
        requestCode: Int = 0,
        flags: Int = PendingIntent.FLAG_UPDATE_CURRENT,
        vararg parentLaunchers: BaseIntentLauncher

    ): PendingIntent {
        val list =
            listOf(*parentLaunchers.map { it.getIntent(context) }.toTypedArray(), getIntent(context))

        val intents = list.toTypedArray()

        return PendingIntent.getActivities(context, requestCode, intents, flags)
    }

    fun getPendingIntentWithParentStack(
        context: Context,
        requestCode: Int = 0,
        flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
    ): PendingIntent {
        return TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(getIntent(context))
            .getPendingIntent(requestCode, flags)!!
    }

    fun launchForResult(activity: Activity, requestCode: Int, option: Bundle? = null) {
        activity.compatStartActivityForResult(getIntent(activity), requestCode, option)
    }

    fun launchForResult(fragment: Fragment, requestCode: Int, option: Bundle? = null) {
        fragment.startActivityForResult(getIntent(fragment.context!!), requestCode, option)
    }

    protected fun Context.compatStartActivity(intent: Intent, option: Bundle? = null) {
        ContextCompat.startActivity(this, intent, option)
    }

    protected fun Activity.compatStartActivityForResult(intent: Intent, requestCode: Int, option: Bundle? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            startActivityForResult(intent, requestCode, option)
        } else {
            startActivityForResult(intent, requestCode)
        }
    }


}