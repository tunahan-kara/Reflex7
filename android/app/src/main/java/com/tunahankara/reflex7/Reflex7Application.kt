package com.tunahankara.reflex7

import android.app.Application
import com.tunahankara.reflex7.data.PreferencesRepository

class Reflex7Application : Application() {
    val preferencesRepository by lazy { PreferencesRepository(this) }
}
