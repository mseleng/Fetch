@file:JvmName("FetchErrorUtils")

package com.tonyodev.fetch2

import com.tonyodev.fetch2core.*
import java.io.IOException
import java.net.SocketTimeoutException

fun getErrorFromThrowable(throwable: Throwable): Error {
    var message = throwable.message ?: ""
    if (throwable is SocketTimeoutException && message.isEmpty()) {
        message = CONNECTION_TIMEOUT
    }
    val error = getErrorFromMessage(message)
    return when {
        error == Error.UNKNOWN && throwable is SocketTimeoutException -> Error.CONNECTION_TIMED_OUT
        error == Error.UNKNOWN && throwable is IOException -> Error.UNKNOWN_IO_ERROR
        else -> error
    }
}

fun getErrorFromMessage(message: String?): Error {
    return if (message == null || message.isEmpty()) {
        Error.UNKNOWN
    } else if (message.contains(UNIQUE_ID_DATABASE)) {
        Error.REQUEST_WITH_ID_ALREADY_EXIST
    } else if (message.equals(EMPTY_RESPONSE_BODY, true)) {
        Error.EMPTY_RESPONSE_FROM_SERVER
    } else if (message.equals(FNC, ignoreCase = true) || message.equals(ENOENT, ignoreCase = true)) {
        Error.FILE_NOT_CREATED
    } else if (message.contains(ETIMEDOUT, ignoreCase = true)
            || message.contains(CONNECTION_TIMEOUT, ignoreCase = true)
            || message.contains(SOFTWARE_ABORT_CONNECTION, ignoreCase = true)
            || message.contains(READ_TIME_OUT, ignoreCase = true)) {
        Error.CONNECTION_TIMED_OUT
    } else if (message.equals(IO404, ignoreCase = true) || message.contains(NO_ADDRESS_HOSTNAME)) {
        Error.HTTP_NOT_FOUND
    } else if (message.contains(HOST_RESOLVE_ISSUE)) {
        Error.UNKNOWN_HOST
    } else if (message.equals(EACCES, ignoreCase = true)) {
        Error.WRITE_PERMISSION_DENIED
    } else if (message.equals(ENOSPC, ignoreCase = true)
            || message.equals(DATABASE_DISK_FULL, ignoreCase = true)) {
        Error.NO_STORAGE_SPACE
    } else if (message.equals(FAILED_TO_ENQUEUE_REQUEST, true)) {
        Error.REQUEST_ALREADY_EXIST
    } else if (message.equals(DOWNLOAD_NOT_FOUND, true)) {
        Error.DOWNLOAD_NOT_FOUND
    } else if (message.equals(FETCH_DATABASE_ERROR, true)) {
        Error.FETCH_DATABASE_ERROR
    } else if (message.contains(RESPONSE_NOT_SUCCESSFUL, true) || message.contains(FAILED_TO_CONNECT, true)) {
        Error.REQUEST_NOT_SUCCESSFUL
    } else if (message.contains(INVALID_CONTENT_MD5, true)) {
        Error.INVALID_CONTENT_MD5
    } else {
        Error.UNKNOWN
    }

}