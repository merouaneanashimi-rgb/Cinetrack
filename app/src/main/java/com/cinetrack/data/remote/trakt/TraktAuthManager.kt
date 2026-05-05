package com.cinetrack.data.remote.trakt

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import com.cinetrack.BuildConfig

private val Context.traktDataStore: DataStore<Preferences> by preferencesDataStore(name = "trakt_auth")

@Singleton
class TraktAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val traktApiService: TraktApiService
) {
    companion object {
        val CLIENT_ID     = if (BuildConfig.TRAKT_CLIENT_ID.isNotEmpty()) BuildConfig.TRAKT_CLIENT_ID else "4e1c74f04393a15e9cb7ec8bc86de1aa62eaeed368a4231c1418c45bf3803e7f"
        val CLIENT_SECRET = if (BuildConfig.TRAKT_CLIENT_SECRET.isNotEmpty()) BuildConfig.TRAKT_CLIENT_SECRET else "41badee254df6c21160eaa4f578e0b53af52656ec1d8e8c2f7e0d483393dc272"
        const val REDIRECT_URI  = "cinetrack://trakt/oauth"
        const val BASE_AUTH_URL = "https://trakt.tv/oauth/authorize"
        const val API_BASE_URL  = "https://api.trakt.tv/"

        private val KEY_ACCESS_TOKEN  = stringPreferencesKey("trakt_access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("trakt_refresh_token")
        private val KEY_EXPIRES_AT    = longPreferencesKey("trakt_expires_at")
        private val KEY_USERNAME      = stringPreferencesKey("trakt_username")
        private val KEY_AVATAR        = stringPreferencesKey("trakt_avatar")
        private val KEY_LAST_SYNC     = longPreferencesKey("trakt_last_sync")
    }

    private val store = context.traktDataStore

    // ─── Token State ──────────────────────────────────────────────────────────

    val isConnected: Flow<Boolean> = store.data.map { prefs ->
        !prefs[KEY_ACCESS_TOKEN].isNullOrBlank()
    }

    val username: Flow<String?> = store.data.map { it[KEY_USERNAME] }
    val avatar: Flow<String?>   = store.data.map { it[KEY_AVATAR] }
    val lastSync: Flow<Long>    = store.data.map { it[KEY_LAST_SYNC] ?: 0L }

    suspend fun getAccessToken(): String? {
        val prefs = store.data.first()
        val token = prefs[KEY_ACCESS_TOKEN] ?: return null
        val expiresAt = prefs[KEY_EXPIRES_AT] ?: 0L
        // Refresh if within 7 days of expiry
        return if (System.currentTimeMillis() > expiresAt - 7 * 24 * 3600 * 1000L) {
            refreshTokenInternal(prefs[KEY_REFRESH_TOKEN] ?: return null)
        } else {
            token
        }
    }

    fun getBearerToken(raw: String) = "Bearer $raw"

    // ─── OAuth Flow ───────────────────────────────────────────────────────────

    fun buildAuthUrl(): String {
        return Uri.parse(BASE_AUTH_URL).buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .build().toString()
    }

    fun openAuthPage(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(buildAuthUrl()))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    suspend fun handleCallback(code: String): Result<Unit> {
        return try {
            val response = traktApiService.exchangeCode(
                TraktTokenRequest(
                    code = code,
                    clientId = CLIENT_ID,
                    clientSecret = CLIENT_SECRET,
                    redirectUri = REDIRECT_URI
                )
            )
            if (response.isSuccessful) {
                val token = response.body()!!
                saveToken(token)
                fetchAndSaveProfile()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Auth failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun refreshTokenInternal(refreshToken: String): String? {
        return try {
            val response = traktApiService.refreshToken(
                TraktRefreshRequest(
                    refreshToken = refreshToken,
                    clientId = CLIENT_ID,
                    clientSecret = CLIENT_SECRET,
                    redirectUri = REDIRECT_URI
                )
            )
            if (response.isSuccessful) {
                val token = response.body()!!
                saveToken(token)
                token.accessToken
            } else null
        } catch (e: Exception) { null }
    }

    private suspend fun saveToken(token: TraktTokenResponse) {
        store.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN]  = token.accessToken
            prefs[KEY_REFRESH_TOKEN] = token.refreshToken
            prefs[KEY_EXPIRES_AT]    = (token.createdAt + token.expiresIn) * 1000L
        }
    }

    private suspend fun fetchAndSaveProfile() {
        try {
            val raw = store.data.first()[KEY_ACCESS_TOKEN] ?: return
            val response = traktApiService.getUserProfile("Bearer $raw")
            if (response.isSuccessful) {
                response.body()?.let { profile ->
                    store.edit { prefs ->
                        prefs[KEY_USERNAME] = profile.username
                        profile.avatar?.full?.let { prefs[KEY_AVATAR] = it }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    suspend fun disconnect() {
        try {
            val raw = store.data.first()[KEY_ACCESS_TOKEN]
            if (!raw.isNullOrBlank()) {
                traktApiService.revokeToken(mapOf("token" to raw, "client_id" to CLIENT_ID))
            }
        } catch (_: Exception) {}
        store.edit { it.clear() }
    }

    suspend fun updateLastSync() {
        store.edit { it[KEY_LAST_SYNC] = System.currentTimeMillis() }
    }
}
