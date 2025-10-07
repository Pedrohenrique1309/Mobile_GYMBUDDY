package senai.sp.jandira.mobile_gymbuddy.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object UserPreferences {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_NICKNAME = "user_nickname"
    private const val KEY_USER_EMAIL = "user_email"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserData(context: Context, id: Int, name: String, nickname: String, email: String) {
        val prefs = getPrefs(context)
        with(prefs.edit()) {
            putInt(KEY_USER_ID, id)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_NICKNAME, nickname)
            putString(KEY_USER_EMAIL, email)
            apply()
        }
        Log.d("UserPreferences", "Dados salvos - ID: $id, Nome: $name, Nickname: $nickname")
    }

    fun getUserId(context: Context): Int {
        return getPrefs(context).getInt(KEY_USER_ID, 1)
    }

    fun getUserName(context: Context): String {
        return getPrefs(context).getString(KEY_USER_NAME, "Usuário") ?: "Usuário"
    }

    fun getUserNickname(context: Context): String {
        return getPrefs(context).getString(KEY_USER_NICKNAME, "user") ?: "user"
    }

    fun getUserEmail(context: Context): String {
        return getPrefs(context).getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun clearUserData(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    fun isUserLoggedIn(context: Context): Boolean {
        return getPrefs(context).contains(KEY_USER_ID)
    }
}
