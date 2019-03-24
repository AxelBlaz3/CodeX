package com.codebot.axel.codex

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_change_log_items.view.*

class ChangeLogAdapter(val context: Context, codexInfo: CodexInfo) : RecyclerView.Adapter<ChangeLogViewHolder>() {

    private var changeLog: ArrayList<String> = ArrayList()
    private val sizeOfChangelog = codexInfo.changelog.size

    init {
        for (i in 0 until sizeOfChangelog) {
            val sizeOfArray = codexInfo.changelog[i].added.size
            val currentChangelog = codexInfo.changelog[i].added
            for (j in 0 until sizeOfArray) {
                changeLog.add(currentChangelog[j])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChangeLogViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val currentRow = layoutInflater.inflate(R.layout.activity_change_log_items, parent, false)
        return ChangeLogViewHolder(currentRow)
    }

    override fun onBindViewHolder(holder: ChangeLogViewHolder, position: Int) {
        if (changeLog[position].contains("Version", false)) {
            val string = Html.fromHtml("<h2>" + "<font color=${ContextCompat.getColor(context, R.color.colorAccent)}>" + changeLog[position] + "</font></h2>").trim()
            holder.view.changelog_bullet_textView.visibility = View.GONE
            holder.view.changelog_added_textView.text = string
        } else
            holder.view.changelog_added_textView.text = changeLog[position]

    }

    override fun getItemCount(): Int {
        return changeLog.size
    }

}

class ChangeLogViewHolder(val view: View) : RecyclerView.ViewHolder(view)