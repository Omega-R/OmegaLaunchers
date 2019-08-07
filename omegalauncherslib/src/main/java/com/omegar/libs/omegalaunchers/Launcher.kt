package com.omegar.libs.omegalaunchers

import android.content.Context

/**
 * Created by Anton Knyazev on 06.04.2019.
 */
interface Launcher {

    fun launch(context: Context)

    interface DefaultCompanion {

        fun createLauncher(): Launcher

        fun launch(context: Context) {
            createLauncher()
                .launch(context)
        }

    }

}