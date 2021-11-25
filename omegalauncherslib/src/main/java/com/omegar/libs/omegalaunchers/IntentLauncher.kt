package com.omegar.libs.omegalaunchers

import android.content.Context
import android.content.Intent
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anton Knyazev on 2019-08-09.
 */
@Parcelize
class IntentLauncher(private val intent: Intent) : BaseIntentLauncher() {

    override fun getIntent(context: Context) = intent

}