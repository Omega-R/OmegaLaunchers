package com.omegar.libs.omegalaunchers

import android.content.Context
import android.content.Intent

/**
 * Created by Anton Knyazev on 2019-08-09.
 */
class IntentLauncher(private val intent: Intent) : BaseIntentLauncher() {

    override fun getIntent(context: Context) = intent

}