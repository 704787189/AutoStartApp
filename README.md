# 安卓开机自启动APP

## 功能说明

本APP实现以下功能：
1. **开机自启动**：设备启动后自动运行本APP
2. **倒计时10秒**：显示倒计时界面
3. **再等待5秒**：内部延迟
4. **启动目标应用**：自动启动指定的APP

## 目标应用配置

需要修改 `MainActivity.java` 中的以下常量：

```java
private static final String TARGET_PACKAGE = "com.hinacom.miviewzfp";
private static final String TARGET_ACTIVITY = "com.example.miviewzfp.MainActivity";
```

## 编译步骤

### 方法一：使用Android Studio（推荐）

1. 用Android Studio打开 `AutoStartApp` 文件夹
2. 选择 "Import project"
3. 等待Gradle同步完成
4. 点击 Build > Build APK
5. 在 `app/build/outputs/apk` 目录下找到生成的APK

### 方法二：使用命令行

1. 确保已安装Android SDK
2. 在项目根目录执行：
```bash
gradlew assembleDebug
```

## 安装APK

将生成的APK复制到安卓设备，安装后授予以下权限：
- 开机自启动权限
- 读取手机状态权限

## 注意事项

- 本APP针对Android 4.4.4 (API 19) 开发
- 首次运行需要手动打开一次以激活自启动功能
- 目标应用 `com.hinacom.miviewzfp` 需要已安装

## 项目结构

```
AutoStartApp/
├── app/
│   ├── build.gradle          # 应用模块配置
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/autostartapp/
│       │   ├── BootReceiver.java   # 开机广播接收器
│       │   └── MainActivity.java   # 主界面（倒计时逻辑）
│       └── res/
│           ├── layout/activity_main.xml
│           └── values/strings.xml
├── build.gradle              # 项目根配置
└── settings.gradle
```
