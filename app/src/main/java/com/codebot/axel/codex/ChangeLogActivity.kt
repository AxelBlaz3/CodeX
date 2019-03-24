package com.codebot.axel.codex

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_change_log.*

class ChangeLogActivity : AppCompatActivity() {

    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_log)

        val codexData = intent.getSerializableExtra("codexData") as CodexInfo

        pref = getSharedPreferences(getString(R.string.key_custom_pref), Context.MODE_PRIVATE)

        changelog_progressBar.isIndeterminate = true
        changelog_progressBar.visibility = View.VISIBLE
        fetchJSON(this, codexData)
    }

    private fun fetchJSON(context: Context, codexData: CodexInfo) {
        if (!Utils().isNetworkAvailable(context)) {
            if (pref.getString(getString(R.string.changelog_json), "NA") == "NA")
                Utils().noNetworkDialog(context)
            else
                loadChangelogFromPreferences()
        } else {
            runOnUiThread {
                changelog_progressBar.visibility = View.GONE
                recyclerView.adapter = ChangeLogAdapter(context, codexData)
                recyclerView.layoutManager = LinearLayoutManager(context)
            }
        }
    }

    private fun loadChangelogFromPreferences() {
        val jsonBody = pref.getString(getString(R.string.changelog_json), "NA")
        val gson = GsonBuilder().create()
        val codexInfo = gson.fromJson(jsonBody, CodexInfo::class.java)
        recyclerView.adapter = ChangeLogAdapter(this, codexInfo)
        recyclerView.layoutManager = LinearLayoutManager(this)

    }
}
