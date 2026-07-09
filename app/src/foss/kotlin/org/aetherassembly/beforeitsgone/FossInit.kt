package org.aetherassembly.beforeitsgone

import androidx.activity.ComponentActivity
import org.woheller69.freeDroidWarn.FreeDroidWarn

object FossInit {
    fun showWarnings(activity: ComponentActivity, versionCode: Int) {
        FreeDroidWarn.showWarningOnUpgrade(activity, versionCode)
    }
}
