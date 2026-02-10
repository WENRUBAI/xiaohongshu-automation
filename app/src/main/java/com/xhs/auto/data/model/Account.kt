package com.xhs.auto.data.model

import java.util.UUID

data class Account(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastRunTime: Long? = null,
    val status: AccountStatus = AccountStatus.AVAILABLE,
    val xiaohongshuUid: String? = null
) {
    enum class AccountStatus {
        AVAILABLE,      // 可用
        RUNNING,        // 运行中
        ERROR           // 异常
    }
    
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "isDefault" to isDefault,
            "createdAt" to createdAt,
            "lastRunTime" to lastRunTime,
            "status" to status.name,
            "xiaohongshuUid" to xiaohongshuUid
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any?>): Account {
            return Account(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                name = map["name"] as? String ?: "",
                isDefault = map["isDefault"] as? Boolean ?: false,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                lastRunTime = (map["lastRunTime"] as? Number)?.toLong(),
                status = AccountStatus.valueOf(map["status"] as? String ?: AccountStatus.AVAILABLE.name),
                xiaohongshuUid = map["xiaohongshuUid"] as? String
            )
        }
    }
}
