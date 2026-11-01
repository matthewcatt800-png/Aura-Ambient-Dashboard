package com.aura.ambientdashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.aura.ambientdashboard.data.AuraPreferences
import com.aura.ambientdashboard.databinding.ActivityMainBinding
import com.aura.ambientdashboard.ui.ambient.AmbientFragment
import com.aura.ambientdashboard.ui.home.HomeFragment
import com.aura.ambientdashboard.ui.settings.SettingsFragment
import com.aura.ambientdashboard.ui.system.SystemFragment
import com.aura.ambientdashboard.ui.widgets.WidgetsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: AuraPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = AuraPreferences(applicationContext)
        AppCompatDelegate.setDefaultNightMode(nightModeFor(prefs.themeMode))
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener { item ->
            selectTab(item.itemId)
            true
        }
        binding.bottomNav.setOnItemReselectedListener { /* stay put */ }
        binding.bottomNav.selectedItemId = validTab(prefs.selectedTab)
    }

    private fun selectTab(itemId: Int) {
        prefs.selectedTab = itemId
        when (itemId) {
            R.id.nav_ambient -> show(AmbientFragment(), "ambient")
            R.id.nav_widgets -> show(WidgetsFragment(), "widgets")
            R.id.nav_system -> show(SystemFragment(), "system")
            R.id.nav_settings -> show(SettingsFragment(), "settings")
            else -> show(HomeFragment(), "home")
        }
    }

    private fun show(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .commit()
    }

    private fun validTab(itemId: Int): Int = when (itemId) {
        R.id.nav_home, R.id.nav_ambient, R.id.nav_widgets, R.id.nav_system, R.id.nav_settings -> itemId
        else -> R.id.nav_home
    }

    private fun nightModeFor(mode: String): Int = when (mode) {
        "dark" -> AppCompatDelegate.MODE_NIGHT_YES
        "light" -> AppCompatDelegate.MODE_NIGHT_NO
        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
}
