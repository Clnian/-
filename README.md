# 自动记账 Android 应用

## 项目概述
这是一个使用Java开发的Android自动记账应用，支持Android 7.0及以上版本，使用Gradle 8.11.1构建。

## 主要功能
1. **手动记账** - 用户可以手动添加收入和支出记录
2. **自动记账** - 通过解析银行和支付平台短信自动创建交易记录
3. **分类管理** - 支持多种收支分类
4. **统计功能** - 按时间段查看收支统计
5. **数据存储** - 使用Room数据库本地存储数据

## 技术架构
- **架构模式**: MVVM (Model-View-ViewModel)
- **数据库**: Room + SQLite
- **UI框架**: Material Design
- **开发语言**: Java
- **最低支持**: Android 7.0 (API 24)
- **构建工具**: Gradle 8.11.1

## 项目结构
```
app/src/main/java/com/autoexpense/tracker/
├── data/
│   ├── entity/          # 数据实体类
│   ├── dao/             # 数据访问对象
│   ├── database/        # Room数据库
│   ├── repository/      # 数据仓库
│   └── converter/       # 类型转换器
├── ui/
│   ├── viewmodel/       # ViewModel类
│   ├── adapter/         # RecyclerView适配器
│   ├── MainActivity.java
│   └── AddTransactionActivity.java
├── receiver/
│   └── SmsReceiver.java # 短信接收器
└── utils/
    └── SmsParser.java   # 短信解析工具
```

## 核心组件

### 数据层
- **Transaction**: 交易记录实体
- **Category**: 分类实体
- **TransactionDao**: 交易数据访问接口
- **CategoryDao**: 分类数据访问接口
- **AppDatabase**: Room数据库主类

### UI层
- **MainActivity**: 主界面，显示统计信息和交易列表
- **AddTransactionActivity**: 添加交易记录界面
- **TransactionAdapter**: 交易记录列表适配器
- **MainViewModel**: 主界面的ViewModel

### 自动记账
- **SmsReceiver**: 监听短信的广播接收器
- **SmsParser**: 解析银行短信的工具类

## 权限要求
- `RECEIVE_SMS`: 接收短信权限（用于自动记账）
- `READ_SMS`: 读取短信权限（用于自动记账）
- `INTERNET`: 网络权限（预留）

## 安装和运行
1. 确保Android Studio已安装
2. 克隆或下载项目代码
3. 在Android Studio中打开项目
4. 等待Gradle同步完成
5. 连接Android设备或启动模拟器
6. 点击运行按钮

## 使用说明
1. **首次启动**: 应用会请求短信权限，建议授予以启用自动记账功能
2. **手动记账**: 点击右下角的"+"按钮添加交易记录
3. **查看统计**: 在主界面可以查看不同时间段的收支统计
4. **自动记账**: 当收到银行或支付平台的交易短信时，应用会自动解析并创建记录

## 支持的银行和支付平台
- 支付宝
- 微信支付
- 主要银行（工行、建行、农行、中行等）

## 注意事项
1. 自动记账功能需要短信权限
2. 短信解析基于常见的银行短信格式，可能需要根据实际情况调整
3. 建议定期备份数据
4. 首次使用时会自动创建默认分类

## 开发环境
- Android Studio 2023.1.1+
- JDK 8+
- Android SDK 34
- Gradle 8.11.1

## 许可证
本项目仅供学习和参考使用。