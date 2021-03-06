package com.tonyodev.fetch2

import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2core.InterruptMonitor
import com.tonyodev.fetch2core.getFileMd5String
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Collections
import kotlin.collections.HashMap

/**
 * The default Downloader used by Fetch for downloading requests.
 * This downloader uses a HttpUrlConnection to perform http requests
 * @see {@link com.tonyodev.fetch2core.Downloader}
 * */
open class HttpUrlConnectionDownloader @JvmOverloads constructor(
        /**
         * HttpUrlConnectionPreferences to set preference settings for the
         * HttpUrlConnectionDownloader HttpUrlConnection client.
         * @see com.tonyodev.fetch2.HttpUrlConnectionDownloader.HttpUrlConnectionPreferences
         * */
        httpUrlConnectionPreferences: HttpUrlConnectionPreferences? = null,
        /** The file downloader type used to download a request.
         * The SEQUENTIAL type downloads bytes in sequence.
         * The PARALLEL type downloads bytes in parallel.
         * */
        private val fileDownloaderType: Downloader.FileDownloaderType = Downloader.FileDownloaderType.SEQUENTIAL) : Downloader {

    constructor(fileDownloaderType: Downloader.FileDownloaderType) : this(null, fileDownloaderType)

    protected val connectionPrefs = httpUrlConnectionPreferences ?: HttpUrlConnectionPreferences()
    protected val connections: MutableMap<Downloader.Response, HttpURLConnection> = Collections.synchronizedMap(HashMap<Downloader.Response, HttpURLConnection>())

    override fun execute(request: Downloader.ServerRequest, interruptMonitor: InterruptMonitor?): Downloader.Response? {
        val httpUrl = URL(request.url)
        val client = httpUrl.openConnection() as HttpURLConnection
        client.requestMethod = "GET"
        client.readTimeout = connectionPrefs.readTimeout
        client.connectTimeout = connectionPrefs.connectTimeout
        client.useCaches = connectionPrefs.usesCache
        client.defaultUseCaches = connectionPrefs.usesDefaultCache
        client.instanceFollowRedirects = connectionPrefs.followsRedirect
        client.doInput = true
        request.headers.entries.forEach {
            client.addRequestProperty(it.key, it.value)
        }
        client.connect()
        val code = client.responseCode
        var success = false
        var contentLength = -1L
        var byteStream: InputStream? = null
        var md5 = ""
        if (isResponseOk(code)) {
            success = true
            contentLength = client.getHeaderField("Content-Length")?.toLong() ?: -1
            byteStream = client.inputStream
            md5 = client.getHeaderField("Content-MD5") ?: ""
        }
        val response = Downloader.Response(
                code = code,
                isSuccessful = success,
                contentLength = contentLength,
                byteStream = byteStream,
                request = request,
                md5 = md5)

        connections[response] = client
        return response
    }

    protected fun isResponseOk(responseCode: Int): Boolean {
        return responseCode in 200..299
    }

    override fun disconnect(response: Downloader.Response) {
        if (connections.contains(response)) {
            val client = connections[response] as HttpURLConnection
            connections.remove(response)
            disconnectClient(client)
        }
    }

    override fun close() {
        connections.entries.forEach {
            disconnectClient(it.value)
        }
        connections.clear()
    }

    private fun disconnectClient(client: HttpURLConnection?) {
        try {
            client?.disconnect()
        } catch (e: Exception) {

        }
    }

    override fun getRequestOutputStream(request: Downloader.ServerRequest, filePointerOffset: Long): OutputStream? {
        return null
    }

    override fun getFileSlicingCount(request: Downloader.ServerRequest, contentLength: Long): Int? {
        return null
    }

    override fun getDirectoryForFileDownloaderTypeParallel(request: Downloader.ServerRequest): String? {
        return null
    }

    override fun getFileDownloaderType(request: Downloader.ServerRequest): Downloader.FileDownloaderType {
        return fileDownloaderType
    }

    override fun seekOutputStreamToPosition(request: Downloader.ServerRequest, outputStream: OutputStream, filePointerOffset: Long) {

    }

    override fun verifyContentMD5(request: Downloader.ServerRequest, md5: String): Boolean {
        if (md5.isEmpty()) {
            return true
        }
        val fileMd5 = getFileMd5String(request.file)
        return fileMd5?.contentEquals(md5) ?: true
    }

    /**
     * Use this class to set preference settings for the
     * HttpUrlConnectionDownloader HttpUrlConnection client.
     * */
    open class HttpUrlConnectionPreferences {
        var readTimeout = 20_000
        var connectTimeout = 15_000
        var usesCache = false
        var usesDefaultCache = false
        var followsRedirect = true
    }

}