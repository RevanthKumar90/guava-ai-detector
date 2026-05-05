package com.example.proj_guava

import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Root layout
        val listView = ListView(this)

        // Modern clean background
        listView.setBackgroundColor(Color.parseColor("#F5F7FA"))
        listView.setPadding(24, 24, 24, 24)
        listView.dividerHeight = 16

        setContentView(listView)

        val prefs = getSharedPreferences("history", MODE_PRIVATE)

        // SAFE (no crash)
        val data = prefs.getStringSet("data", setOf())?.toList() ?: listOf()

        if (data.isEmpty()) {
            val emptyView = TextView(this)
            emptyView.text = "No scan history yet"
            emptyView.textSize = 18f
            emptyView.setTextColor(Color.GRAY)
            emptyView.setPadding(40, 200, 40, 40)
            emptyView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

            setContentView(emptyView)
            return
        }

        // Custom adapter look (still simple)
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            data
        ) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent) as TextView

                // Style each item
                view.setBackgroundColor(Color.WHITE)
                view.setPadding(30, 30, 30, 30)
                view.textSize = 16f
                view.setTextColor(Color.BLACK)

                return view
            }
        }

        listView.adapter = adapter
    }
}