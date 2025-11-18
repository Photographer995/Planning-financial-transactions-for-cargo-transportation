package com.example.finlogcalc.utils.chat

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.DrawableRes
import com.example.finlogcalc.R

class ChatBackgroundManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("chat_prefs", Context.MODE_PRIVATE)

    fun saveSelectedBackground(@DrawableRes imageRes: Int) {
        prefs.edit().putInt("selected_background", imageRes).apply()
    }

    @DrawableRes
    fun getSelectedBackground(): Int {
        return prefs.getInt("selected_background", R.drawable.nyako5)
    }
}