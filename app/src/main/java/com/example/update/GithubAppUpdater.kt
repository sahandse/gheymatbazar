package com.example.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

data class GithubReleaseInfo(
    val tagName: String,
    val versionName: String,
    val releaseName: String,
    val apkUrl: String,
    val apkSizeBytes: Long,
    val releaseNotes: String
)

sealed class UpdateCheckResult {
    data class UpToDate(val currentVersion: String) : UpdateCheckResult()
    data class Available(val release: GithubReleaseInfo) : UpdateCheckResult()
    data class Failed(val message: String) : UpdateCheckResult()
}

object GithubAppUpdater {

    private const val OWNER = "sahandse"
    private const val REPO = "gheymatbazar"
    private const val LATEST_URL = "https://api.github.com/repos/$OWNER/$REPO/releases/latest"

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdate(): UpdateCheckResult = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(LATEST_URL)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "GheymatBazar/${BuildConfig.VERSION_NAME}")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext UpdateCheckResult.Failed("خطا در ارتباط با GitHub (${response.code})")
                }
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) {
                    return@withContext UpdateCheckResult.Failed("پاسخ خالی از GitHub")
                }
                val json = JSONObject(body)
                val tag = json.optString("tag_name").ifBlank {
                    return@withContext UpdateCheckResult.Failed("تگ نسخه یافت نشد")
                }
                val versionName = tag.removePrefix("v").trim()
                val assets = json.optJSONArray("assets")
                    ?: return@withContext UpdateCheckResult.Failed("فایل انتشار یافت نشد")

                var apkUrl: String? = null
                var apkSize = 0L
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name")
                    if (name.endsWith(".apk", ignoreCase = true) &&
                        name.contains("PRODUCTION", ignoreCase = true)
                    ) {
                        apkUrl = asset.optString("browser_download_url")
                        apkSize = asset.optLong("size")
                        break
                    }
                }
                if (apkUrl.isNullOrBlank()) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkUrl = asset.optString("browser_download_url")
                            apkSize = asset.optLong("size")
                            break
                        }
                    }
                }
                if (apkUrl.isNullOrBlank()) {
                    return@withContext UpdateCheckResult.Failed("APK در ریلیز GitHub نیست")
                }

                val release = GithubReleaseInfo(
                    tagName = tag,
                    versionName = versionName,
                    releaseName = json.optString("name").ifBlank { tag },
                    apkUrl = apkUrl,
                    apkSizeBytes = apkSize,
                    releaseNotes = json.optString("body").orEmpty()
                )

                if (isRemoteNewer(versionName, BuildConfig.VERSION_NAME)) {
                    UpdateCheckResult.Available(release)
                } else {
                    UpdateCheckResult.UpToDate(BuildConfig.VERSION_NAME)
                }
            }
        }.getOrElse {
            UpdateCheckResult.Failed(it.message ?: "بررسی به‌روزرسانی ناموفق بود")
        }
    }

    fun isRemoteNewer(remoteVersion: String, localVersion: String): Boolean {
        val remote = parseVersion(remoteVersion)
        val local = parseVersion(localVersion)
        val max = maxOf(remote.size, local.size)
        for (i in 0 until max) {
            val r = remote.getOrElse(i) { 0 }
            val l = local.getOrElse(i) { 0 }
            if (r != l) return r > l
        }
        return false
    }

    private fun parseVersion(raw: String): List<Int> =
        raw.removePrefix("v")
            .substringBefore("-")
            .split(".")
            .map { it.filter(Char::isDigit).toIntOrNull() ?: 0 }

    /**
     * Downloads APK via DownloadManager into app-external files, then prompts install.
     */
    fun downloadAndInstall(
        context: Context,
        release: GithubReleaseInfo,
        onStarted: (downloadId: Long) -> Unit,
        onError: (String) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            onError("ALLOW_UNKNOWN_SOURCES")
            return
        }

        try {
            val updatesDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "updates")
            if (!updatesDir.exists()) updatesDir.mkdirs()
            val fileName = "gheymatbazar-${release.versionName}.apk"
            val destFile = File(updatesDir, fileName)
            if (destFile.exists()) destFile.delete()

            val request = DownloadManager.Request(release.apkUrl.toUri())
                .setTitle("به‌روزرسانی قیمت بازار ${release.versionName}")
                .setDescription("در حال دانلود از GitHub…")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(destFile))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = dm.enqueue(request)
            onStarted(downloadId)

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: return
                    if (id != downloadId) return
                    runCatching { context.unregisterReceiver(this) }
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    dm.query(query)?.use { cursor ->
                        if (!cursor.moveToFirst()) {
                            onError("دانلود کامل نشد")
                            return
                        }
                        val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val status = cursor.getInt(statusIdx)
                        if (status != DownloadManager.STATUS_SUCCESSFUL) {
                            onError("دانلود ناموفق بود")
                            return
                        }
                    }
                    installApk(context, destFile, onError)
                }
            }

            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (e: Exception) {
            onError(e.message ?: "شروع دانلود ممکن نیست")
        }
    }

    fun openUnknownSourcesSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            "package:${context.packageName}".toUri()
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openReleasePage(context: Context) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "https://github.com/$OWNER/$REPO/releases/latest".toUri()
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun installApk(context: Context, file: File, onError: (String) -> Unit) {
        if (!file.exists() || file.length() < 1024) {
            onError("فایل APK نامعتبر است")
            return
        }
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            onError(e.message ?: "نصب اجرا نشد")
        }
    }
}
