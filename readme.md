# 轻语云图库后端 (Qingyu Picture Backend)

## 项目概述

青鱼图片库后端是一个用于管理和存储图片的后端服务。它提供了用户注册、登录、图片上传、图片查询、图片审核等功能。项目使用了
Spring Boot、MyBatis Plus、Hutool 等技术栈，并集成了 Redis 作为缓存，使用 Knife4j 生成接口文档。

## 主要功能

- **用户管理**：
    - 用户注册
    - 用户登录
    - 用户登出
    - 用户信息增删查改

- **图片管理**：
    - 图片上传（支持文件上传和 URL 上传）
    - 图片批量上传（管理员通过关键词批量爬取）
    - 图片增删查改（支持分页和条件查询）
    - 图片审核

- **缓存管理**：
    - 支持本地缓存（Caffeine）、分布式缓存（Redis）、多级缓存等不同策略

- **接口文档**：
    - 使用 Knife4j 生成详细的接口文档

## 技术栈

- **Spring Boot**: 用于构建 Web 应用
- **MyBatis Plus**: 用于数据库操作
- **Hutool**: 提供各种工具类
- **Knife4j**: 用于生成接口文档
- **Jsoup**: 用于 HTML 解析
- **Redis**: 用于缓存
- **Caffeine**: 用于本地缓存
- **MySQL**: 作为数据库
- **Lombok**: 用于简化代码
- **Tencent COS**: 用于对象存储（可选）

## 项目前端
[https://github.com/LiqingyuDev/qingyu-picture-frontend](https://github.com/LiqingyuDev/qingyu-picture-frontend)

## 快速开始

### 前提条件

- Java 8 或更高版本
- MySQL 5.7 或更高版本
- Redis 6.0 或更高版本

### 安装和运行

1. **克隆项目**
2. **配置数据库**

    - 运行create_table.sql，创建数据库 `qingyu_picture`。
    - 修改 `src/main/resources/application.yml` 中的数据库连接信息。

3. **配置 Redis**

    - 确保 Redis 服务正在运行。
    - 修改 `src/main/resources/application.yml` 中的 Redis 连接信息。

4. **配置腾讯云对象存储**

    - 修改 `src/main/resources/application.yml` 中的 COS 连接信息。
5. **运行项目**

直接运行 `QingyuPictureBackendApplication.java`。

6. **访问接口文档**

   打开浏览器，访问 `http://localhost:8123/api/doc.html` 查看接口文档。

## 代码贡献

我们非常欢迎您的贡献！请遵循以下步骤：

1. Fork本项目
2. 创建您的特性分支 (`git checkout -b my-new-feature`)
3. 提交您的改动 (`git commit -am 'Add some feature'`)
4. 推送到您的分支 (`git push origin my-new-feature`)
5. 创建一个新的Pull Request

## 许可证

本项目采用[MIT许可证](https://opensource.org/licenses/MIT)，请确保您遵守相关条款。

## 联系方式

如有问题或建议，请通过以下方式联系项目维护者：

- **开发者**: 轻语
- **邮箱**: liqingyu.dev@gmail.com
- **GitHub后端**: [qingyu-picture-backend](https://github.com/LiqingyuDev/qingyu-picture-backend)
- **GitHub前端**: [qingyu-picture-frontend](https://github.com/username/qingyu-picture-frontend)

---
   
   

