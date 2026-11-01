package com.aura.ambientdashboard.utilities

import android.view.View

fun View.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

fun View.dpFloat(value: Float): Int = (value * resources.displayMetrics.density).toInt()
