package com.ibm.rides.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ResourceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getString(resId: Int): String {
        return context.getString(resId)
    }
}