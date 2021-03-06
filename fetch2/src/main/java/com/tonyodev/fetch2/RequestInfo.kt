package com.tonyodev.fetch2

import com.tonyodev.fetch2.util.*

/**
 * A RequestInfo allows you to update an existing download managed by Fetch.
 * Note: The fields in this class will overwrite the corresponding fields for an
 * existing download. Be sure to update all the fields in this class with
 * the proper values.
 * */
open class RequestInfo {

    /** Can be used to set your own unique identifier for the request.*/
    var identifier: Long = DEFAULT_UNIQUE_IDENTIFIER

    /** The group id this download belongs to.*/
    var groupId: Int = DEFAULT_GROUP_ID

    /** The headers used by the downloader to send header information to
     * the server about a request.*/
    val headers: MutableMap<String, String> = mutableMapOf()

    /** The download Priority of this download.
     * @see com.tonyodev.fetch2.Priority */
    var priority: Priority = defaultPriority

    /** The network type this download is allowed to download on.
     * @see com.tonyodev.fetch2.NetworkType*/
    var networkType: NetworkType = defaultNetworkType

    /** Adds a header for a download.
     * @param key Header Key
     * @param value Header Value
     * */
    fun addHeader(key: String, value: String) {
        this.headers[key] = value
    }

    /** Associate a tag for this request*/
    var tag: String? = null

    /**
     * Action used by Fetch when enqueuing a request and a previous request with the
     * same file is already being managed. Default EnqueueAction.REPLACE_EXISTING
     * which will replaces the existing request.
     * */
    var enqueueAction = defaultEnqueueAction

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RequestInfo
        if (identifier != other.identifier) return false
        if (groupId != other.groupId) return false
        if (headers != other.headers) return false
        if (priority != other.priority) return false
        if (networkType != other.networkType) return false
        if (tag != other.tag) return false
        if (enqueueAction != other.enqueueAction) return false
        return true
    }

    override fun hashCode(): Int {
        var result = identifier.hashCode()
        result = 31 * result + groupId
        result = 31 * result + headers.hashCode()
        result = 31 * result + priority.hashCode()
        result = 31 * result + networkType.hashCode()
        result = 31 * result + (tag?.hashCode() ?: 0)
        result = 31 * result + enqueueAction.hashCode()
        return result
    }

    override fun toString(): String {
        return "RequestInfo(identifier=$identifier, groupId=$groupId, headers=$headers, priority=$priority, " +
                "networkType=$networkType, tag=$tag, enqueueAction=$enqueueAction)"
    }

}