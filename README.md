# Campus Trade - 校园二手交易平台

基于 Spring Boot 3.2.0 构建的校园二手交易平台，为学生提供便捷的二手物品交易服务。

## 技术栈

- **框架**: Spring Boot 3.2.0
- **语言**: Java 17
- **数据库**: MySQL 8.0+
- **持久层**: MyBatis-Plus 3.5.5
- **连接池**: Druid 1.2.18
- **缓存**: Redis
- **消息队列**: RabbitMQ
- **安全**: Spring Security
- **模板引擎**: Thymeleaf
- **邮件服务**: Spring Mail
- **工具类**: Hutool 5.8.23
- **代码简化**: Lombok

## 项目结构

```
src/main/java/com/campus/trade/
├── CampusTradeApplication.java    # 启动类
├── config/                        # 配置类
│   ├── SecurityConfig.java        # 安全配置
│   ├── RedisConfig.java           # Redis配置
│   ├── RabbitMQConfig.java        # RabbitMQ配置
│   ├── MyBatisPlusConfig.java     # MyBatis-Plus配置
│   └── ...
├── controller/                    # 控制器层
│   ├── HomeController.java        # 首页
│   ├── ArticleController.java     # 商品管理
│   ├── OrderController.java       # 订单管理
│   ├── UserController.java        # 用户管理
│   ├── AdminController.java       # 后台管理
│   └── ...
├── service/                       # 服务层
│   ├── impl/                      # 服务实现
│   └── ...
├── mapper/                        # 数据访问层
├── entity/                        # 实体类
├── dto/                           # 数据传输对象
├── exception/                     # 异常处理
└── constant/                      # 常量定义

src/main/resources/
├── application.yml                # 应用配置
├── mapper/                        # MyBatis映射文件
├── templates/                     # Thymeleaf模板
│   ├── admin/                     # 后台管理页面
│   ├── user/                      # 用户页面
│   └── ...
└── i18n/                          # 国际化资源文件
```

## 功能特性

### 用户功能

- 用户注册、登录（支持记住我）
- 个人信息管理（头像、昵称、联系方式）
- 多学校支持，用户可选择所在学校

### 商品功能

- 商品发布（标题、描述、价格、分类、标签、图片）
- 商品浏览（首页展示、分类筛选、搜索）
- 商品详情查看
- 商品收藏
- 浏览历史记录

### 交易功能

- 购物车管理（添加、删除、数量调整）
- 订单创建与支付
- 订单状态管理（待支付、待发货、已发货、已完成、已取消）
- 订单评价

### 互动功能

- 商品评论（支持多级回复）
- 评论点赞

### 后台管理

- 用户管理（查看、禁用、编辑）
- 商品管理（审核、编辑、删除）
- 分类管理（增删改查）
- 标签管理（增删改查）
- 订单管理（查看订单状态、处理订单）
- 学校管理（增删改查）

### 系统功能

- 邮件通知（商品发布通知）
- 异步任务处理
- 定时任务支持
- 国际化支持（中文/英文）

## 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 7.0+
- RabbitMQ 3.10+
- Maven 3.8+

## 快速开始

### 1. 数据库配置

创建数据库并导入初始化脚本：

```sql
CREATE DATABASE campus_trade CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campus_trade;
SOURCE sql/campus_trade.sql;
```

### 2. 修改配置文件

编辑 `src/main/resources/application.yml`，配置数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_trade?useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

### 3. 启动应用

使用 Maven 运行：

```bash
mvn spring-boot:run
```

或打包后运行：

```bash
mvn clean package
java -jar target/xiehh_campus_trade-0.0.1-SNAPSHOT.jar
```

### 4. 访问应用

- 前台首页：`http://localhost:8080/`
- 后台管理：`http://localhost:8080/admin/dashboard`

### 默认账号

| 用户名 | 密码     | 角色 |
|--------|--------|------|
| user001 | 156565 | 普通用户 |
| user004 | 156565 | 管理员 |

## 数据库表结构

### 核心表

| 表名 | 说明 |
|------|------|
| users | 用户表 |
| article | 商品表 |
| category | 分类表 |
| tag | 标签表 |
| shopping_cart | 购物车表 |
| orders | 订单表 |
| favorite | 收藏表 |
| comment | 评论表 |
| browse_history | 浏览历史表 |
| school | 学校表 |

## 配置说明

### 文件上传路径

```yaml
file:
  upload:
    path: D:/campus_trade_files
```

### 邮件配置

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 465
    username: your_email@qq.com
    password: your_email_password
```

## 开发规范

- 代码风格遵循阿里巴巴 Java 开发规范
- 使用 Lombok 简化实体类代码
- Service 层使用接口 + 实现类模式
- 异常处理使用全局异常处理器
- 日志使用 Logback

## License

MIT License