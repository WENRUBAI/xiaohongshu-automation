package com.xhs.auto.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xhs.auto.data.model.Account

class AccountManager private constructor(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "account_prefs"
        private const val KEY_ACCOUNTS = "accounts"
        private const val MAX_ACCOUNTS = 10
        
        @Volatile
        private var instance: AccountManager? = null
        
        fun getInstance(context: Context): AccountManager {
            return instance ?: synchronized(this) {
                instance ?: AccountManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    fun addAccount(name: String): Result<Account> {
        val accounts = getAccounts().toMutableList()
        
        if (accounts.size >= MAX_ACCOUNTS) {
            return Result.failure(Exception("账号数量已达上限（$MAX_ACCOUNTS个）"))
        }
        
        if (accounts.any { it.name == name }) {
            return Result.failure(Exception("账号名称已存在"))
        }
        
        val isFirstAccount = accounts.isEmpty()
        val account = Account(
            name = name,
            isDefault = isFirstAccount
        )
        
        accounts.add(account)
        saveAccounts(accounts)
        
        return Result.success(account)
    }
    
    fun deleteAccount(accountId: String): Result<Unit> {
        val accounts = getAccounts().toMutableList()
        val account = accounts.find { it.id == accountId }
            ?: return Result.failure(Exception("账号不存在"))
        
        accounts.remove(account)
        
        // 如果删除的是默认账号，将第一个账号设为默认
        if (account.isDefault && accounts.isNotEmpty()) {
            accounts[0] = accounts[0].copy(isDefault = true)
        }
        
        saveAccounts(accounts)
        return Result.success(Unit)
    }
    
    fun setDefaultAccount(accountId: String): Result<Unit> {
        val accounts = getAccounts().toMutableList()
        
        accounts.forEachIndexed { index, account ->
            accounts[index] = account.copy(isDefault = account.id == accountId)
        }
        
        saveAccounts(accounts)
        return Result.success(Unit)
    }
    
    fun getAccounts(): List<Account> {
        val json = prefs.getString(KEY_ACCOUNTS, null) ?: return emptyList()
        val type = object : TypeToken<List<Account>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun getDefaultAccount(): Account? {
        return getAccounts().find { it.isDefault }
    }
    
    fun getAccountById(accountId: String): Account? {
        return getAccounts().find { it.id == accountId }
    }
    
    fun updateAccountStatus(accountId: String, status: Account.AccountStatus): Result<Unit> {
        val accounts = getAccounts().toMutableList()
        val index = accounts.indexOfFirst { it.id == accountId }
        
        if (index == -1) {
            return Result.failure(Exception("账号不存在"))
        }
        
        accounts[index] = accounts[index].copy(status = status)
        saveAccounts(accounts)
        return Result.success(Unit)
    }
    
    fun updateLastRunTime(accountId: String): Result<Unit> {
        val accounts = getAccounts().toMutableList()
        val index = accounts.indexOfFirst { it.id == accountId }
        
        if (index == -1) {
            return Result.failure(Exception("账号不存在"))
        }
        
        accounts[index] = accounts[index].copy(lastRunTime = System.currentTimeMillis())
        saveAccounts(accounts)
        return Result.success(Unit)
    }
    
    private fun saveAccounts(accounts: List<Account>) {
        val json = gson.toJson(accounts)
        prefs.edit().putString(KEY_ACCOUNTS, json).apply()
    }
    
    fun getAccountCount(): Int {
        return getAccounts().size
    }
    
    fun hasAccount(): Boolean {
        return getAccountCount() > 0
    }
}
