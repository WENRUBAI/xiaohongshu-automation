# GitHub Actions 自动构建指南

使用 GitHub Actions 免费自动构建 APK，无需安装任何开发环境。

---

## 快速开始（3步）

### 第 1 步：创建 GitHub 账号和仓库

1. 访问 https://github.com
2. 点击 **Sign up** 注册账号（如果已有账号直接登录）
3. 点击右上角 **+** 号 → **New repository**
4. 填写信息：
   - Repository name: `xiaohongshu-automation`
   - Description: `小红书养号助手 - 自动化工具`
   - 选择 **Public**（免费）
   - 勾选 **Add a README file**
5. 点击 **Create repository**

### 第 2 步：上传代码

#### 方法一：使用 Git 命令行（推荐）

在 Windows PowerShell 中执行：

```powershell
# 进入项目目录
cd E:\biancheng\ruanjian\xiaohongshu\xiaohongshu-automation

# 初始化 Git 仓库
git init

# 添加所有文件
git add .

# 提交代码
git commit -m "Initial commit: 小红书养号助手"

# 添加远程仓库（将 YOUR_USERNAME 替换为您的GitHub用户名）
git remote add origin https://github.com/YOUR_USERNAME/xiaohongshu-automation.git

# 推送代码
git push -u origin main
```

如果提示 `git` 命令不存在，先安装 Git：
- 下载地址：https://git-scm.com/download/win
- 安装时一路点击 Next 即可

#### 方法二：使用 GitHub Desktop（图形界面）

1. 下载 GitHub Desktop：https://desktop.github.com
2. 安装并登录您的 GitHub 账号
3. 点击 **File → Add local repository**
4. 选择项目文件夹：`E:\biancheng\ruanjian\xiaohongshu\xiaohongshu-automation`
5. 点击 **Publish repository**

#### 方法三：直接网页上传（最简单，但不推荐大项目）

1. 在 GitHub 仓库页面点击 **Add file → Upload files**
2. 拖拽项目文件到网页（注意：.gitignore 文件不会被上传）
3. 点击 **Commit changes**

### 第 3 步：获取 APK 文件

1. 推送代码后，GitHub 会自动开始构建
2. 在仓库页面点击 **Actions** 标签
3. 等待构建完成（约 5-10 分钟）
4. 点击最新的工作流运行记录
5. 在页面底部找到 **Artifacts** 区域
6. 下载 `app-debug` 文件（这就是 APK）

---

## 详细操作截图说明

### 1. 创建仓库

```
GitHub 首页 → 右上角 + → New repository

填写：
☑ Repository name: xiaohongshu-automation
☑ Description: 小红书养号助手
☑ Public（选择公开，免费）
☑ Add a README file（勾选）

点击：Create repository
```

### 2. 查看构建状态

```
进入仓库 → Actions 标签 → Build APK

状态说明：
🟡 黄色 = 正在构建
🟢 绿色 = 构建成功
🔴 红色 = 构建失败
```

### 3. 下载 APK

```
点击成功的构建记录 → 页面底部 Artifacts

下载：
- app-debug.apk（调试版本，推荐测试用）
- app-release-unsigned.apk（发布版本，未签名）
```

---

## 常见问题

### Q1: 推送代码时提示权限错误

**解决：**
```powershell
# 方法 1：使用 HTTPS + 个人访问令牌
git remote set-url origin https://YOUR_TOKEN@github.com/YOUR_USERNAME/xiaohongshu-automation.git

# 方法 2：使用 SSH（需要配置密钥）
git remote set-url origin git@github.com:YOUR_USERNAME/xiaohongshu-automation.git
```

生成个人访问令牌：
1. GitHub → Settings → Developer settings → Personal access tokens
2. 点击 Generate new token
3. 勾选 repo 权限
4. 复制生成的令牌作为密码使用

### Q2: 构建失败怎么办？

**查看日志：**
1. 进入 Actions 页面
2. 点击失败的构建记录
3. 查看红色 ❌ 标记的步骤
4. 查看错误日志

**常见错误：**
- Gradle 版本不兼容 → 已配置好，一般不会有问题
- 代码语法错误 → 检查 Kotlin 代码
- 依赖下载失败 → 重新运行构建

### Q3: 如何更新代码并重新构建？

```powershell
# 修改代码后，执行：
git add .
git commit -m "更新说明"
git push

# GitHub 会自动触发新的构建
```

### Q4: APK 下载后如何安装？

1. 将 APK 文件传输到手机
2. 手机上点击 APK 文件
3. 允许安装未知来源应用
4. 完成安装

---

## 构建配置说明

### 自动触发条件

`.github/workflows/build.yml` 配置：

```yaml
on:
  push:
    branches: [ main, master ]    # 推送到 main/master 分支时触发
  pull_request:
    branches: [ main, master ]    # 提交 PR 时触发
  workflow_dispatch:              # 允许手动触发
```

**手动触发方法：**
1. 进入 Actions 页面
2. 点击 **Build APK**
3. 点击 **Run workflow**
4. 选择分支，点击 **Run workflow**

### 构建产物

| 文件 | 说明 | 用途 |
|------|------|------|
| app-debug.apk | 调试版本 | 开发测试用，包含调试信息 |
| app-release-unsigned.apk | 发布版本（未签名） | 需要签名后才能发布到应用商店 |

---

## 安全提示

⚠️ **重要提醒：**

1. **Public 仓库**：任何人都能看到您的代码
   - 不要提交敏感信息（密码、密钥等）
   - 代码中的配置都是本地存储，相对安全

2. **GitHub Actions 免费额度**：
   - 公共仓库：无限次免费构建
   - 单次构建时长限制：6小时
   - 存储限制：500MB（构建产物）

3. **APK 文件**：
   - 构建产物保留 7 天
   - 及时下载保存
   - 不要分享给不信任的人

---

## 下一步

获取 APK 后：

1. **安装到手机**
   - 传输 APK 到手机
   - 允许安装未知来源应用
   - 安装完成

2. **开启无障碍服务**
   - 设置 → 无障碍 → 小红书养号助手
   - 开启服务

3. **配置使用**
   - 添加账号
   - 调整配置（可选）
   - 开始任务

---

## 需要帮助？

如果遇到问题：

1. 查看 Actions 页面的错误日志
2. 检查代码是否有语法错误
3. 在 GitHub Issues 中搜索类似问题
4. 重新推送代码触发新的构建

---

**祝您使用愉快！** 🎉
