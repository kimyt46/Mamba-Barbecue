\# 曼巴烧烤 Mamba-Barbecue 项目本地运行指南

本项目为SpringBoot实战后台管理系统，本文档为\*\*本地一键启动完整教程\*\*

\## 一、项目环境要求

| 软件 | 版本要求 | 说明 |

| ---- | ---- | ---- |

| IntelliJ IDEA | 2021及以上版本 | 开发运行工具 |

| JDK | 17 | 项目编译运行环境 |

| MySQL |  8.0 | 项目数据库 |

| Maven | IDEA内置即可 | 项目依赖管理 |

\---

\## 二、本地运行详细步骤

\### 步骤1：IDEA中打开项目

1\. 打开 `IntelliJ IDEA` 

2\. 点击首页 `Open`，选择项目根目录文件夹 `reggie\_take\_out`

3\. 等待IDEA自动加载Maven依赖，等待右下角依赖下载完成

4\. 等待项目源码目录自动识别（java源码目录蓝色、resources资源目录黄色）

\### 步骤2：安装配置`MySQL`数据库，`DataGrip`并导入`.SQL`文件

> MYSQL配置教程：https://blog.csdn.net/m0_67703159/article/details/139874886

1. 本地安装并启动 \*\*MySQL\*\* 数据库服务并启动，配置

2. 在`Datagrip`中管理数据库，将项目内置的SQL脚本文件导入MySQL数据库

3. 修改项目配置文件 `application.yml` 中数据库连接信息（数据库名、用户名、密码），匹配本地MySQL配置

\### 步骤3：启动SpringBoot项目并访问前台页面

1\. 在项目中找到启动类 `ReggieApplication.java`（含main主方法）

2\. 右键启动类，点击 `Run` 启动SpringBoot项目

3\. 等待控制台启动成功、无报错信息

4\. 打开浏览器，访问前台地址：http://127.0.0.1:8080/backend/page/login/login.html
