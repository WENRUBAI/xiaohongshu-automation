package com.xhs.auto.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.xhs.auto.data.AccountManager
import com.xhs.auto.data.model.Account
import com.xhs.auto.databinding.ActivityAccountBinding

class AccountActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAccountBinding
    private lateinit var accountManager: AccountManager
    private lateinit var adapter: AccountAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        accountManager = AccountManager.getInstance(this)
        
        setupUI()
        loadAccounts()
    }
    
    private fun setupUI() {
        // 返回按钮
        binding.btnBack.setOnClickListener { finish() }
        
        // 添加账号按钮
        binding.btnAddAccount.setOnClickListener {
            showAddAccountDialog()
        }
        
        // 设置RecyclerView
        adapter = AccountAdapter(
            onSetDefault = { account ->
                accountManager.setDefaultAccount(account.id)
                loadAccounts()
                Toast.makeText(this, "已设为默认账号", Toast.LENGTH_SHORT).show()
            },
            onDelete = { account ->
                showDeleteDialog(account)
            }
        )
        
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }
    
    private fun loadAccounts() {
        val accounts = accountManager.getAccounts()
        adapter.submitList(accounts)
    }
    
    private fun showAddAccountDialog() {
        val editText = android.widget.EditText(this)
        editText.hint = "请输入账号名称"
        
        AlertDialog.Builder(this)
            .setTitle("添加账号")
            .setView(editText)
            .setPositiveButton("添加") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    val result = accountManager.addAccount(name)
                    if (result.isSuccess) {
                        loadAccounts()
                        Toast.makeText(this, "账号添加成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, result.exceptionOrNull()?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun showDeleteDialog(account: Account) {
        AlertDialog.Builder(this)
            .setTitle("删除账号")
            .setMessage("确定要删除账号 \"${account.name}\" 吗？")
            .setPositiveButton("删除") { _, _ ->
                accountManager.deleteAccount(account.id)
                loadAccounts()
                Toast.makeText(this, "账号已删除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}

// 简化的Adapter类
class AccountAdapter(
    private val onSetDefault: (Account) -> Unit,
    private val onDelete: (Account) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<AccountAdapter.ViewHolder>() {
    
    private var accounts: List<Account> = emptyList()
    
    fun submitList(list: List<Account>) {
        accounts = list
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = accounts[position]
        holder.bind(account)
    }
    
    override fun getItemCount() = accounts.size
    
    inner class ViewHolder(itemView: android.view.View) : 
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        
        private val text1: android.widget.TextView = itemView.findViewById(android.R.id.text1)
        private val text2: android.widget.TextView = itemView.findViewById(android.R.id.text2)
        
        fun bind(account: Account) {
            text1.text = account.name + if (account.isDefault) " (默认)" else ""
            text2.text = "状态: ${account.status.name}"
            
            itemView.setOnClickListener {
                showAccountOptions(account)
            }
        }
        
        private fun showAccountOptions(account: Account) {
            val options = if (account.isDefault) {
                arrayOf("删除")
            } else {
                arrayOf("设为默认", "删除")
            }
            
            AlertDialog.Builder(itemView.context)
                .setTitle(account.name)
                .setItems(options) { _, which ->
                    when (options[which]) {
                        "设为默认" -> onSetDefault(account)
                        "删除" -> onDelete(account)
                    }
                }
                .show()
        }
    }
}
