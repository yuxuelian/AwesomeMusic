package com.kaibo.music.player.utils

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Preference<T>(val context: Context, val name: String, private val default: T, private val prefName: String) : ReadWriteProperty<Any?, T> {

    private val prefs by lazy {
        if (prefName.isNotEmpty()) {
            context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        } else {
            PreferenceManager.getDefaultSharedPreferences(context)
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return findPreference(findProperName(property))
    }

    private fun findProperName(property: KProperty<*>) = if (name.isEmpty()) {
        property.name
    } else {
        name
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    private fun findPreference(key: String): T {
        @Suppress("UNCHECKED_CAST")
        return when (default) {
            is Int -> prefs.getInt(key, default)
            is Long -> prefs.getLong(key, default)
            is Float -> prefs.getFloat(key, default)
            is Boolean -> prefs.getBoolean(key, default)
            is String -> prefs.getString(key, default)
            else -> prefs.getString(key, null)
        } as T
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        putPreference(findProperName(property), value)
    }

    @SuppressLint("CommitPrefEdits")
    private fun putPreference(key: String, value: T) {
        with(prefs.edit()) {
            when (value) {
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is Boolean -> putBoolean(key, value)
                is String -> putString(key, value)
                else -> putString(key, null)
            }
        }.commit()
    }
}

inline fun <reified R, T> R.pref(context: Context, default: T, prefName: String = "") = Preference(context, "", default, prefName)
