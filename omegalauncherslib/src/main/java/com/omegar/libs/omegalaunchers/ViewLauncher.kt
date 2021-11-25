package com.omegar.libs.omegalaunchers

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import kotlinx.android.parcel.Parcelize

@Parcelize
class ViewLauncher(
    private val viewClass: Class<View>,
    @IdRes private val layoutContainerId: Int? = null,
    private val width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    private val height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
) : Launcher {

    companion object {
        private val CONSTRUCTOR_SIGNATURE = arrayOf(Context::class.java)
    }

    override fun launch(context: Context) {
        if (context is Activity) {
            val createdView = createView(context)
            val layoutParams = ViewGroup.LayoutParams(width, height)
            if (layoutContainerId != null) {
                val viewGroup = context.findViewById<ViewGroup>(layoutContainerId)
                viewGroup.addView(createdView, layoutParams)
            } else {
                context.addContentView(createdView, layoutParams)
            }
        } else {
            throw IllegalAccessException("No supported")
        }
    }

    fun createView(context: Context): View = viewClass
        .getConstructor(*CONSTRUCTOR_SIGNATURE)
        .newInstance(context)

}