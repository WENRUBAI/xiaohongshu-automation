# 🚀 快速开始指南

## 目标：获取 APK 文件并安装到手机

---

## 方案：使用 GitHub Actions（推荐）

**优势：** 完全免费，无需安装任何软件，自动构建

---

## 步骤一：准备代码（5分钟）

### 1. 注册 GitHub 账号
- 访问 https://github.com
- 点击 **Sign up** 注册
- 验证邮箱

### 2. 创建仓库
- 登录 GitHub
- 点击右上角 **+** → **New repository**
- Repository name: `xiaohongshu-automation`
- 选择 **Public**
- 点击 **Create repository**

### 3. 上传代码

**方法一：使用 Git（推荐）**

下载并安装 Git：https://git-scm.com/download/win

打开 PowerShell，执行：

```powershell
cd E:\biancheng\ruanjian\xiaohongshu\xiaohongshu-automation

git init

git add .

git commit -m "Initial commit"

git remote add origin https://github.com/你的用户名/xiaohongshu-automation.git

git push -u origin main
```

**方法二：使用 GitHub Desktop（图形界面）**

1. 下载：https://desktop.github.com
2. 安装并登录
3. File → Add local repository
4. 选择项目文件夹
5. 点击 Publish repository

---

## 步骤二：自动构建（10分钟）

### 1. 等待构建
- 推送代码后，GitHub 会自动开始构建
- 进入仓库页面 → **Actions** 标签
- 等待约 5-10 分钟

### 2. 下载 APK
- 构建完成后（显示绿色 ✓）
- 点击最新的构建记录
- 页面底部找到 **Artifacts**
- 下载 `app-debug`（这就是 APK 文件）

---

## 步骤三：安装到手机（5分钟）

### 1. 传输 APK
- 将下载的 APK 文件传输到手机
- 可以通过微信、QQ、数据线等方式

### 2. 安装应用
- 在手机上找到 APK 文件
- 点击安装
- 允许安装未知来源应用（系统会提示）

### 3. 开启无障碍服务
- 打开 **设置 → 无障碍**
- 找到 **小红书养号助手**
- 开启服务

### 4. 开始使用
- 打开小红书 App，确保已登录
- 打开养号助手 App
- 添加账号
- 点击 **开始任务**

---

## 📋 检查清单

- [ ] 注册 GitHub 账号
- [ ] 创建仓库
- [ ] 上传代码
- [ ] 等待构建完成
- [ ] 下载 APK
- [ ] 安装到手机
- [ ] 开启无障碍服务
- [ ] 开始使用

---

## ❓ 常见问题

**Q: 构建失败怎么办？**
A: 检查代码是否有语法错误，或重新推送触发新构建

**Q: 无法推送代码？**
A: 需要生成个人访问令牌作为密码

**Q: APK 安装失败？**
A: 确保允许安装未知来源应用

---

## 📖 详细文档

- 完整指南：[GITHUB_GUIDE.md](GITHUB_GUIDE.md)
- 项目说明：[README.md](README.md)

---

**预计总时间：20-30 分钟**

**需要帮助？** 查看详细指南或搜索 GitHub Actions 教程
