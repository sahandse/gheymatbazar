package com.example.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
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
    private const val RELEASES_URL = "https://api.github.com/repos/$OWNER/$REPO/releases?per_page=10"
    private const val MIN_APK_BYTES = 1_000_000L

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdate(): UpdateCheckResult = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(RELEASES_URL)
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

                val releases = JSONArray(body)
                var newestInstallable: GithubReleaseInfo? = null

                for (i in 0 until releases.length()) {
                    val json = releases.getJSONObject(i)
                    if (json.optBoolean("draft") || json.optBoolean("prerelease")) continue

                    val tag = json.optString("tag_name").trim()
                    if (tag.isBlank()) continue
                    val versionName = tag.removePrefix("v").trim()
                    if (versionName.isBlank()) continue

                    val assets = json.optJSONArray("assets") ?: continue
                    var selectedUrl: String? = null
                    var selectedSize = 0L

                    // Prefer a production APK so debug/test artifacts are never offered to users.
                    for (index in 0 until assets.length()) {
                        val asset = assets.getJSONObject(index)
                        val name = asset.optString("name")
                        if (name.endsWith(".apk", ignoreCase = true) &&
                            name.contains("PRODUCTION", ignoreCase = true)
                        ) {
                            selectedUrl = asset.optString("browser_download_url")
                            selectedSize = asset.optLong("size")
                            break
                        }
                    }

                    // Backward compatibility for older releases that only contain one APK.
                    if (selectedUrl.isNullOrBlank()) {
                        for (index in 0 until assets.length()) {
                            val asset = assets.getJSONObject(index)
                            val name = asset.optString("name")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                selectedUrl = asset.optString("browser_download_url")
                                selectedSize = asset.optLong("size")
                                break
                            }
                        }
                    }

                    if (selectedUrl.isNullOrBlank() || selectedSize < MIN_APK_BYTES) continue
                    if (!selectedUrl.startsWith("https://github.com/")) continue

                    newestInstallable = GithubReleaseInfo(
                        tagName = tag,
                        versionName = versionName,
                        releaseName = json.optString("name").ifBlank { tag },
                        apkUrl = selectedUrl,
                        apkSizeBytes = selectedSize,
                        releaseNotes = json.optString("body").orEmpty()
                    )
                    break
                }

                val release = newestInstallable
                    ?: return@withContext UpdateCheckResult.Failed("هنوز APK قابل نصب در Releaseهای GitHub وجود ندارد")

                if (isRemoteNewer(release.versionName, BuildConfig.VERSION_NAME)) {
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

    fun formatSize(bytes: Long): String {
        if (bytes <= 0L) return "—"
        val mb = bytes.toDouble() / (1024.0 * 1024.0)
        return String.format(java.util.Locale.US, "%.1f MB", mb)
    }

    fun downloadAndInstall(
        context: Context,
        release: GithubReleaseInfo,
        onStarted: (downloadId: Long) -> Unit,
        onProgress: (Int) -> Unit = {},
        onReadyToInstall: () -> Unit = {},
        onError: (String) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            onError("ALLOW_UNKNOWN_SOURCES")
            return
        }

        try {
            val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: run {
                    onError("حافظه دانلود در دسترس نیست")
                    return
                }
            val updatesDir = File(baseDir, "updates").apply { mkdirs() }

            // Keep only the APK currently being downloaded.
            updatesDir.listFiles()?.filter { it.extension.equals("apk", true) }?.forEach { it.delete() }

            val fileName = "gheymatbazar-${release.versionName}-PRODUCTION.apk"
            val destFile = File(updatesDir, fileName)

            val request = DownloadManager.Request(release.apkUrl.toUri())
                .setTitle("به‌روزرسانی قیمت بازار ${release.versionName}")
                .setDescription("در حال دانلود نسخه رسمی…")
                .setMimeType("application/vnd.android.package-archive")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(destFile))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: return
                    if (id <= 0L) return
                    val query = DownloadManager.Query().setFilterById(id)
                    dm.query(query)?.use { cursor ->
                        if (!cursor.moveToFirst()) return
                        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_FAILED) {
                            runCatching { context.unregisterReceiver(this) }
                            onError("دانلود نسخه جدید ناموفق بود")
                            return
                        }
                        if (status != DownloadManager.STATUS_SUCCESSFUL) return
                    }
                    runCatching { context.unregisterReceiver(this) }

                    val minimumExpected = if (release.apkSizeBytes > 0L) {
                        (release.apkSizeBytes * 0.95).toLong()
                    } else MIN_APK_BYTES
                    if (!destFile.exists() || destFile.length() < minimumExpected) {
                        destFile.delete()
                        onError("فایل دانلودشده ناقص است؛ دوباره تلاش کنید")
                        return
                    }

                    onProgress(100)
                    onReadyToInstall()
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

            val downloadId = dm.enqueue(request)
            onStarted(downloadId)
            observeProgress(dm, downloadId, onProgress)
        } catch (e: Exception) {
            onError(e.message ?: "شروع دانلود ممکن نیست")
        }
    }

    private fun observeProgress(
        dm: DownloadManager,
        downloadId: Long,
        onProgress: (Int) -> Unit
    ) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                var keepPolling = false
                runCatching {
                    dm.query(DownloadManager.Query().setFilterById(downloadId))?.use { cursor ->
                        if (!cursor.moveToFirst()) return@use
                        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        val downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (total > 0L) {
                            onProgress(((downloaded * 100L) / total).toInt().coerceIn(0, 100))
                        }
                        keepPolling = status == DownloadManager.STATUS_PENDING ||
                            status == DownloadManager.STATUS_RUNNING ||
                            status == DownloadManager.STATUS_PAUSED
                    }
                }
                if (keepPolling) handler.postDelayed(this, 600L)
            }
        }
        handler.post(runnable)
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
            "https://github.com/$OWNER/$REPO/releases".toUri()
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun installApk(context: Context, file: File, onError: (String) -> Unit) {
        if (!file.exists() || file.length() < MIN_APK_BYTES) {
            onError("فایل APK نامعتبر است")
            return
        }
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                data = uri
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                putExtra(Intent.EXTRA_RETURN_RESULT, false)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            onError(e.message ?: "نصب اجرا نشد")
        }
    }
}
