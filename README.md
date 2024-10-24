# MediaData Kit Sample Code (Android)

English | [中文](README_ZH.md)

## Contents

* [Introduction](#Introduction)
* [Environment Requirements](#Environment-Requirements)
* [Hardware Requirements](#Hardware-Requirements)
* [Preparations](#Preparations)
* [Installation](#Installation)
* [Technical Support](#Technical-Support)
* [License](#License)

## Introduction

In this sample code, you will use the created demo project to call APIs of MediaData Kit. Through the demo project, you will:
1. Access classified albums (place album, portrait album, thing album, collection album).
2. Smart search for pictures and videos.
3. Access live photo.

For more information, please refer to [Service Introduction](https://developer.honor.com/cn/docs/11032/guides/introduction).

## Environment Requirements

Android targetSdkVersion 29 or later and JDK 1.8 or later are recommended.

## Hardware Requirements

A computer (desktop or laptop) running Windows 10 or Windows 7
A Honor MagicOS 8.0 and above phones with USB data cable, which is used for debugging.

## Preparations
1.	Register as a Honor developer.
2.	Create an app and start APIs.
3.	Import your demo project to Android Studio (Chipmunk | 2021.2.1) or later. Download the **mcs-services.json** file of the app from [Honor Developer Site](https://developer.honor.com/), and add the file to the root directory of your project. Generate a signing certificate fingerprint, add the certificate file to your project, and add the configuration to the *build.gradle* file. For details, please refer to the [integration preparations](https://developer.honor.com/cn/docs/11032/guides/intergrate).


## Installation
Method 1: Compile and build the APK in Android Studio. Then, install the APK on your phone and debug it.

Method 2: Generate the APK in Android Studio. Use the Android Debug Bridge (ADB) tool to run the **adb install {*YourPath/YourApp.apk*}** command to install the APK on your phone and debug it.

## Technical Support

If you have any questions about the sample code, try the following:
- Visit [Stack Overflow](https://stackoverflow.com/questions/tagged/honor-developer-services?tab=Votes), submit your questions, and tag them with `honor-developer-services`. Honor experts will answer your questions.

If you encounter any issues when using the sample code, submit your [issues](https://github.com/HONORDevelopers/MediaData-demo/issues) or submit a [pull request](https://github.com/HONORDevelopers/MediaData-demo/pulls).

## License
The sample code is licensed under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).