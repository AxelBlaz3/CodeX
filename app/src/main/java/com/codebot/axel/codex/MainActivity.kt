package com.codebot.axel.codex

import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val KERNEL_VERSION_FULL = System.getProperty("os.version")
    private val KERNEL_VERSION = KERNEL_VERSION_FULL.substring(KERNEL_VERSION_FULL.lastIndexOf('-') + 1, KERNEL_VERSION_FULL.length)
    private var codexJSONUrl = "https://raw.githubusercontent.com/AxelBlaz3/Codex-Kernel/gh-pages/whyred.json"
    private lateinit var preferences: SharedPreferences
    val context = this
    private var flag = false
    private var codexData: CodexInfo? = null
    var navIdHolder: Int = 0
    private var downloadId: Long = 0
    private lateinit var onDownloadComplete: BroadcastReceiver
    private val animation = RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)

    companion object {
        const val CHECK_FOR_UPDATES = "Check For Updates"
        const val DOWNLOAD = "Download"
        const val FLASH = "Flash"
        const val CHANGELOG = "Changelog"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        flag = false

        if (Utils().isNetworkAvailable(context)) {
            Utils().startRefreshAnimation(context, animation)
            fetchJSON(animation, CHECK_FOR_UPDATES)
        }

        onDownloadComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                //Fetching the download id received with the broadcast
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                //Checking if the received broadcast is for our enqueued download by matching download id
                if (downloadId == id) {
                    Toast.makeText(context, "Build downloaded successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }

        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        // Toast.makeText(this, context.packageManager.getPackageInfo(context.packageName, 0).versionName, Toast.LENGTH_SHORT).show()
        if (!Utils().isNetworkAvailable(context)) {
            Utils().noNetworkDialog(context)
        }
        // Verifies if CodeX is absent on the device
        if (KERNEL_VERSION_FULL.contains("CodeX")) {
            alertUser()
        } else {
            preferences = PreferenceManager.getDefaultSharedPreferences(this)

            if (preferences.getBoolean(getString(R.string.key_miui_check), false))
                flag = true

            isStoragePermissionGranted()

            Utils().showDeviceInfo(context, KERNEL_VERSION)

            toggleDrawer()
        }

        changelog_button.setOnClickListener {
            fetchJSON(animation, CHANGELOG)
        }

        download_zip_button.setOnClickListener {
            fetchJSON(animation, DOWNLOAD)
        }

        flash_kernel_button.setOnClickListener {
            fetchJSON(animation, FLASH)
        }

        check_updates_imageView.setOnClickListener {
            Utils().startRefreshAnimation(context, animation)
            fetchJSON(animation, CHECK_FOR_UPDATES)
        }

        nav_view.setNavigationItemSelectedListener(object : NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.nav_settings -> {
                        navIdHolder = R.id.nav_settings
                    }
                    R.id.nav_about -> {
                        navIdHolder = R.id.nav_about
                    }
                }
                drawer_layout.closeDrawer(Gravity.START)
                return true
            }
        })

        drawer_layout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(p0: Int) {}

            override fun onDrawerSlide(p0: View, p1: Float) {}

            override fun onDrawerClosed(p0: View) {
                when (navIdHolder) {
                    R.id.nav_settings -> {
                        navIdHolder = 0
                        val settingsIntent = Intent(this@MainActivity, SettingsActivity::class.java)
                        startActivity(settingsIntent)
                    }
                    R.id.nav_about -> {
                        navIdHolder = 0
                        Utils().showAboutDialog(this@MainActivity)
                    }
                }
            }

            override fun onDrawerOpened(p0: View) {}
        })
    }

    private fun toggleDrawer() {
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun alertUser() {
        val title = "CodeX not found!"
        val message = "Sorry, this app is exclusively made to work with CodeX kernel"
        val buttonText = "OK"
        val finishActivity = true
        Utils().showAlertDialog(context, title, message, buttonText, finishActivity)
    }

    fun checkForUpdates(codexData: CodexInfo?) {
        println("codexJSONUrl: $codexJSONUrl")
        if (Utils().isNetworkAvailable(context)) {
            if (preferences.getBoolean(getString(R.string.key_wifi_only), false)) {
                if (isWifi()) {
                    Utils().isUpdateAvailable(context, codexData, KERNEL_VERSION_FULL, animation)
                } else
                    alertUserForWifi()
            } else
                Utils().isUpdateAvailable(context, codexData, KERNEL_VERSION_FULL, animation)
        } else
            Utils().noNetworkDialog(context)
    }

    private fun isWifi(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (mWifi.isConnected)
            return true
        return false
    }

    private fun alertUserForWifi() {
        val title = "No Wi-Fi Network"
        val message = "You're not on Wi-Fi. Please change the settings of restrict mobile data"
        val buttonText = "OK"
        val finishActivity = false
        Utils().showAlertDialog(context, title, message, buttonText, finishActivity)
    }

    private fun isStoragePermissionGranted(): Boolean {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return true
        else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            return false
        }
    }

    private fun fetchJSON(animation: RotateAnimation, currentTask: String) {
        if (!Utils().isNetworkAvailable(context)) {
            Utils().noNetworkDialog(context)
            Utils().stopRefreshAnimation(animation)
        } else {
            val client = OkHttpClient()
            val request = Request.Builder().url(codexJSONUrl).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                    e!!.printStackTrace()
                }

                override fun onResponse(call: Call?, response: Response?) {
                    val bodyOfJSON = response?.body()?.string()
                    Utils().saveJSONtoPreferences(context, bodyOfJSON)
                    val gson = GsonBuilder().create()
                    codexData = gson.fromJson(bodyOfJSON, CodexInfo::class.java)

                    when (currentTask) {
                        DOWNLOAD -> {
                            runOnUiThread {
                                downloadId = Utils().downloadPackage(context, codexData)
                            }
                        }
                        FLASH -> {
                            runOnUiThread {
                                Utils().rebootAndFlashPackage(context, codexData)
                            }
                        }
                        CHECK_FOR_UPDATES -> {
                            runOnUiThread {
                                latest_version_textView.text = codexData!!.downloads.ver
                                checkForUpdates(codexData)
                            }
                        }
                        CHANGELOG -> {
                            val intent = Intent(this@MainActivity, ChangeLogActivity::class.java)
                            intent.putExtra("codexData", codexData)
                            startActivity(intent)
                        }
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete)
    }
}