## About

This project contains source code for Stickerpipe main module.
You can find complete integration documentation and sample [here](https://github.com/908Inc/stickerpipe-chat-sample)


If you want add stickerfactory module to your existing project, follow next steps
* add stickerfactory repository as submodules
```
git submodule add https://github.com/908Inc/stickerfactory.git
```
* at settings.gradle add
```
include ':app', 'stickerfactory'
```
* at root build.gradle add variables
```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.0.0-alpha2'
    }
}
allprojects {
    repositories {
        jcenter()
    }
}
ext.sdkVersionName = "0.15.0"
ext.minifyEnableMode = true
ext.groupName = "vc908.stickers"
ext.artifactoryUrl = ""
ext.artifactoryUsername = ""
ext.artifactoryPass = ""
```
* At project app build.gradle add dependency
```
dependencies {
    ...
    compile project(':stickerfactory')
}
```

## Contacts

i@stickerpipe.com

## License

Stickerpipe is available under the Apache 2 license. See the [LICENSE](LICENSE) file for more information.
