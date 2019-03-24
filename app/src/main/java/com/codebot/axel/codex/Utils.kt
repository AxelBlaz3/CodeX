package com.codebot.axel.codex

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.RecoverySystem
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Toast
import kotlinx.android.synthetic.main.about_dialog.view.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.dialog_design.view.*
import kotlinx.android.synthetic.main.multi_button_dialog_design.view.*
import java.io.File

class Utils {

    val KERNEL_VERSION_FULL = System.getProperty("os.version")
    val KERNEL_VERSION = KERNEL_VERSION_FULL.substring(KERNEL_VERSION_FULL.lastIndexOf('-') + 1, KERNEL_VERSION_FULL.length)
    var downloadId: Long = 0

    fun showAlertDialog(context: Context, title: String, message: String, buttonDesc: String, finishActivity: Boolean) {
        val builder = AlertDialog.Builder(context)
        val dialogView = View.inflate(context, R.layout.dialog_design, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialogView.dialog_title.text = title
        dialogView.dialog_message.text = message
        dialogView.dialog_button.text = buttonDesc
        val alertDialog = builder.create()
        alertDialog.show()
        dialogView.dialog_button.setOnClickListener {
            alertDialog.dismiss()
            if (finishActivity)
                (context as Activity).finish()
        }
    }

    fun showDeviceInfo(activity: Activity, kernelVersion: String) {
        activity.device_desc_textView.text = Build.MODEL
        activity.codename_desc_textView.text = Build.DEVICE
        activity.kernel_desc_textView.text = kernelVersion
        activity.androidversion_desc_textView.text = Build.VERSION.RELEASE
    }

    fun showAboutDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val dialogView = View.inflate(context, R.layout.about_dialog, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialogView.about_dialog_title.text = context.getString(R.string.app_name)
        dialogView.about_dialog_desc.text = context.getString(R.string.about_kernel)
        dialogView.about_dialog_button.text = "OK"
        val alertDialog = builder.create()
        alertDialog.show()
        dialogView.about_dialog_button.setOnClickListener {
            alertDialog.dismiss()
        }
        dialogView.about_dialog_telegram.setOnClickListener {
            val telegramIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/AxelBlaz3"))
            context.startActivity(telegramIntent)
        }
        dialogView.about_dialog_xda.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://forum.xda-developers.com/redmi-note-5-pro/development/kernel-codex-kernel-v1-0-t3805198"))
            context.startActivity(browserIntent)
        }
    }

    private fun showMultiButtonDialog(context: Context, title: String, message: String, buttonPositive: String, buttonNegative: String, installPackage: String) {
        val builder = AlertDialog.Builder(context)
        val dialogView = View.inflate(context, R.layout.multi_button_dialog_design, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialogView.dialog_multi_title.text = title
        dialogView.dialog_multi_message.text = message
        dialogView.dialog_button_positive.text = buttonPositive
        dialogView.dialog_button_negative.text = buttonNegative
        val alertDialog = builder.create()
        alertDialog.show()
        dialogView.dialog_button_positive.setOnClickListener {
            flashPackage(context, installPackage, alertDialog)
        }
        dialogView.dialog_button_negative.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    fun noNetworkDialog(context: Context) {
        val title = "No Network Available"
        val message = "Please check your network connection and try again"
        val buttonText = "OK"
        val finishActivity = false
        showAlertDialog(context, title, message, buttonText, finishActivity)
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    fun saveJSONtoPreferences(context: Context, bodyOfJSON: String?) {
        if (!bodyOfJSON.equals(null)) {
            val saveResponseStringPref = context.getSharedPreferences(context.getString(R.string.save_response_preference), Context.MODE_PRIVATE)
            saveResponseStringPref.edit().putString(context.getString(R.string.responce_string), bodyOfJSON).apply()
        }
    }

    fun startRefreshAnimation(activity: Activity, animation: RotateAnimation) {
        animation.interpolator = LinearInterpolator()
        animation.repeatCount = Animation.INFINITE
        animation.duration = 1500
        activity.check_updates_imageView.startAnimation(animation)
    }

    fun stopRefreshAnimation(animation: RotateAnimation) {
        animation.cancel()
    }

    fun isUpdateAvailable(context: Context, codexData: CodexInfo?, kernelVersion: String, animation: RotateAnimation) {
        var currentVersion = kernelVersion.substring(kernelVersion.lastIndexOf('-') + 1, kernelVersion.length)
        currentVersion = currentVersion.substring(1, currentVersion.length)
        when {
            currentVersion.matches(Regex(".*[a-z].*")) -> {
                Toast.makeText(context, "Version verificaton is unsupported", Toast.LENGTH_SHORT).show()
            }
            codexData!!.downloads.ver.toDouble() > currentVersion.toDouble() -> {
                (context as Activity).update_notify_textView.setTextColor(context.resources.getColor(R.color.colorAccent))
                context.update_notify_textView.text = "An update is available!"
                Toast.makeText(context, "Update available", Toast.LENGTH_SHORT).show()
            }
            else -> {
                (context as Activity).update_notify_textView.setTextColor(context.resources.getColor(R.color.green))
                context.update_notify_textView.text = "You're up to date."
                Toast.makeText(context, "You're up to date!", Toast.LENGTH_SHORT).show()
            }
        }
        stopRefreshAnimation(animation)
    }

    fun rebootAndFlashPackage(context: Context, codexData: CodexInfo?) {
        val downloadUrl = codexData!!.downloads.url
        val fileToFlash = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1, downloadUrl.length)
        if (fileToFlash.substring(fileToFlash.indexOf('-') + 1, fileToFlash.lastIndexOf('.')) == KERNEL_VERSION) {
            val title = ""
            val message = "You are up to date"
            val buttonText = "OK"
            val finishActivity = false
            showAlertDialog(context, title, message, buttonText, finishActivity)
        } else {
            if (!(File(Environment.getExternalStorageDirectory().toString() + "/CodeX/$fileToFlash").exists())) {
                val title = "No Package Found"
                val message = "Make sure you've downloaded the latest package"
                val buttonText = "OK"
                val finishActivity = false
                showAlertDialog(context, title, message, buttonText, finishActivity)
            } else {
                val title = "Package Found"
                val message = "Reboot and install now?"
                val buttonPositive = "Yes"
                val buttonNegative = "Later"
                showMultiButtonDialog(context, title, message, buttonPositive, buttonNegative, fileToFlash)
            }
        }
    }

    private fun flashPackage(context: Context, packageName: String, dialog: AlertDialog) {
        val installPackage = File(Environment.getExternalStorageDirectory().toString() + "/CodeX/$packageName")
        try {
            RecoverySystem.installPackage(context, installPackage)
            installPackage.delete()
        } catch (e: Exception) {
            dialog.dismiss()
            Log.d("Utils.flashPackage()", "$e")
            Toast.makeText(context, "Make sure that CodeX is a system app", Toast.LENGTH_LONG).show()
        }
    }

    fun downloadPackage(context: Context, codexData: CodexInfo?): Long {
        val downloadUrl = codexData!!.downloads.url
        val downloadFileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1, downloadUrl.length)
        val downloadPath = Environment.getExternalStorageDirectory().toString() + "/CodeX/"

        if (!File(downloadPath).exists())
            File(downloadPath).mkdirs()
        val installPackage = File("$downloadPath$downloadFileName")
        if (installPackage.exists()) {
            val title = "Package Exists"
            val message = "Package already exist on your storage"
            val buttonText = "OK"
            val finishActivity = false
            showAlertDialog(context, title, message, buttonText, finishActivity)
        } else {
            val downloadRequest = DownloadManager.Request(Uri.parse(downloadUrl))
                    .setTitle(downloadFileName)
                    .setDestinationInExternalPublicDir("/CodeX/", downloadFileName)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadId = downloadManager.enqueue(downloadRequest)
            Log.e("Utils", "$downloadId")
            return downloadId
        }
        return -1
    }
}