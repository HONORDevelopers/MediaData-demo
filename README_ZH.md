# 媒体数据服务示例代码

[English](README.md) | 中文

## 目录

 * [简介](#简介)
 * [环境要求](#环境要求)
 * [硬件要求](#硬件要求)
 * [开发准备](#开发准备)
 * [安装](#安装)
 * [技术支持](#技术支持)
 * [授权许可](#授权许可)

## 简介

本示例代码中，你将使用已创建的代码工程来调用荣耀媒体数据服务（MediaData Kit）的接口。通过该工程，你将：
1. 访问分类相册（地点相册、人像相册、事物相册、收藏相册）。
2. 智慧搜索图片、视频。
3. 访问动态照片。

更多内容，请参见[业务简介](https://developer.honor.com/cn/docs/11032/guides/introduction)

## 环境要求

推荐使用的安卓targetSdk版本为29及以上，JDK版本为1.8及以上。

## 硬件要求

安装需要Windows 10/Windows 7操作系统的计算机（台式机或者笔记本），
带USB数据线的荣耀MagicOS 8.0及以上版本手机，用于业务调试。

## 开发准备

1.	注册荣耀帐号，成为荣耀开发者。
2.	创建应用，启动接口。
3.	构建本示例代码，需要先把它导入安卓集成开发环境（Android Studio的版本为2021.2.1及以上）。然后从[荣耀开发者服务平台](https://developer.honor.com/)下载应用的mcs-services.json文件，并添加到对应示例代码根目录下。另外，需要生成签名证书指纹并将证书文件添加到项目中，然后将配置添加到build.gradle。详细信息，请参见[集成指南](https://developer.honor.com/cn/docs/11032/guides/intergrate)集成准备。


## 安装
* 方法1：在Android Studio中进行代码的编译构建。构建APK完成后，将APK安装到手机上，并调试APK。
* 方法2：在Android Studio中生成APK。使用ADB（Android Debug Bridge）工具通过adb install {YourPath/YourApp.apk} 命令将APK安装到手机，并调试APK。

## 技术支持

您可在[荣耀开发者社区](https://developer.honor.com/cn/forum/?navation=dh11614886576872095748%2F1)获取关于MediaData Kit的最新讯息，并与其他开发者交流见解。

开发过程遇到问题上[Stack Overflow](https://stackoverflow.com/questions/tagged/honor-developer-services?tab=Votes)，在`honor-developer-services`标签下提问，有荣耀研发专家在线一对一解决您的问题。

如果您在尝试示例代码中遇到问题，请向仓库提交[issue](https://github.com/HONORDevelopers/MediaData-demo/issues)，也欢迎您提交[Pull Request](https://github.com/HONORDevelopers/MediaData-demo/pulls)。

## 授权许可

该示例代码经过[Apache 2.0授权许可](http://www.apache.org/licenses/LICENSE-2.0)。