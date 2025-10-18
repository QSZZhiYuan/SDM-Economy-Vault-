# SDM-Economy 1.20.1 Forge - 本地构建指南

## ⚠️ 重要提示

由于 Replit 环境的资源限制，Gradle 构建可能超时。建议在本地环境构建。

## 📋 构建要求

- **Java**: 17 或更高版本
- **内存**: 至少 4GB RAM
- **磁盘空间**: 至少 2GB
- **网络**: 稳定的互联网连接（首次构建需下载依赖）

## 🔨 本地构建步骤

### 1. 准备环境

```bash
# 检查 Java 版本
java -version
# 应显示 Java 17 或更高

# 设置 Java 内存（可选）
export GRADLE_OPTS="-Xmx4G"
```

### 2. 下载项目

将整个 `SDM-Economy-1.20.1` 文件夹下载到本地。

### 3. 执行构建

```bash
cd SDM-Economy-1.20.1

# Windows
gradlew.bat :forge:build

# Linux/Mac
./gradlew :forge:build
```

### 4. 跳过测试（更快）

```bash
# Windows
gradlew.bat :forge:build -x test

# Linux/Mac
./gradlew :forge:build -x test
```

### 5. 完整构建（所有平台）

```bash
# Windows
gradlew.bat build

# Linux/Mac
./gradlew build
```

## 📦 构建产物位置

成功构建后，JAR 文件位于：

```
forge/build/libs/SDMEconomy-1.20.1-forge-2.3.0.jar
```

这就是你需要的 Forge 模组文件！

## 🐛 常见问题

### 问题 1: "Permission denied"
```bash
chmod +x gradlew
./gradlew :forge:build
```

### 问题 2: 内存不足
```bash
export GRADLE_OPTS="-Xmx4G -Xms2G"
./gradlew :forge:build --no-daemon
```

### 问题 3: 下载依赖失败
- 检查网络连接
- 使用 VPN（如果在中国）
- 重试构建

### 问题 4: Gradle 缓存损坏
```bash
# 清理缓存
./gradlew clean
rm -rf .gradle build */build

# 重新构建
./gradlew :forge:build
```

## ⏱️ 构建时间

- **首次构建**: 5-15 分钟（下载依赖和映射）
- **后续构建**: 1-3 分钟

## ✅ 验证构建

```bash
# 检查 JAR 文件
ls -lh forge/build/libs/*.jar

# 应该看到类似：
# SDMEconomy-1.20.1-forge-2.3.0.jar
```

## 🚀 部署到服务器

1. 将构建好的 JAR 文件复制到服务器的 `mods/` 文件夹
2. 重启服务器
3. 检查日志确认加载成功

### 混合服务器（Arclight/Mohist）
还需要：
- Vault 插件（Bukkit）
- 启动后查看控制台：
  ```
  [SDM-Economy] Running on hybrid server with Vault support!
  ```

## 📝 构建日志

如果构建失败，查看详细日志：

```bash
./gradlew :forge:build --stacktrace > build.log 2>&1
cat build.log
```

## 🆘 获取帮助

如果构建仍然失败：

1. 检查 Java 版本是否正确
2. 确保有足够的内存和磁盘空间
3. 查看构建日志中的错误信息
4. 尝试使用 `--refresh-dependencies` 重新下载依赖

```bash
./gradlew :forge:build --refresh-dependencies
```

## 📋 验证 Vault 集成

构建成功后，检查 JAR 文件是否包含 Vault 集成：

```bash
# 解压并检查
unzip -l forge/build/libs/SDMEconomy-1.20.1-forge-2.3.0.jar | grep -i vault

# 应该看到：
# net/sixik/sdmeconomy/forge/vault/SDMVaultEconomy.class
# net/sixik/sdmeconomy/forge/vault/VaultIntegration.class
```

## 🎉 构建成功！

构建完成后，你将获得：
- ✅ 完整的 Forge 模组
- ✅ 内置 Vault 集成（混合服务器）
- ✅ 支持所有 SDM-Economy 功能
- ✅ 兼容 SDM-Shop 和 SDM-Market

立即部署到你的 Minecraft 1.20.1 Forge 服务器！
