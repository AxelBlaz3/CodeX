package com.codebot.axel.codex

import android.os.Bundle
import android.preference.PreferenceFragment

/**
 * Created by Axel on 6/8/2018.
 */
class SettingsFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.pref_settings)
    }
}