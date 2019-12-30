# Permission Utils
[![](https://jitpack.io/v/khkong/PermissionUtils.svg)](https://jitpack.io/#khkong/PermissionUtils)
[![API](https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=14) [![GitHub issues](https://img.shields.io/github/issues/khkong/PermissionUtils.svg)](https://github.com/khkong/PermissionUtils/issues)
[![GitHub stars](https://img.shields.io/github/stars/khkong/PermissionUtils.svg)](https://github.com/kyeonghwan-kong/PermissionUtils/stargazers) 
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/khkong/PermissionUtils/master/LICENSE) 

Get runtime permissions on Android quickly and easily.😎

## About Fork
This is Android library for get runtime permissions quickly and easily. I hard forked in Kotlin to make it easier and faster to use. So I refactored and replaced it with simpler, more intuitive code. This code was forked in [Raphaelbussa's PermissionUtils](https://github.com/raphaelbussa/PermissionUtils), which helped to set up the concept and basic structure of the library. It does not provide all of the features provided by the original, and provides simpler functionality.

## How to
To get a Git project into your build:

**Step 1.** Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:
```Gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

**Step 2.** Add the dependency
```Gradle
	dependencies {
	        implementation 'com.github.khkong:PermissionUtils:1.0.0-alpha1'
	}
```
## Sample code

**Step 1.** Create `PermissionManager::class`

`setRetry()` and `setRequestCode()` are optional. Default request code is *100*.

```Kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
	
        permissionManager = PermissionManager.Builder()
            .addPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission_group.CAMERA
            )
            .setRetry(2)
            .setRequestCode(100)
            .whenAllPass {
                doNextProcess()
            }
            .whenDenied { deniedPermissions, foreverDeniedPermissions ->
                goCustomPageForPermissionRequest()
            }
            .build()
    }
}
```

**Step 2.** Call `PermissionManager.request()`
```Kotlin
    override fun onResume() {
        super.onResume()
        permissionManager.request(this)
    }
```

**Step 3.** Override `onRequestPermissionsResult()`. and call `PermissionManager.handleResult()`

just insert a single line of code!

```Kotlin
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.handleResult(this@MainActivity, requestCode, permissions, grantResults)
    }
```

**Little extra**. You can also take the following actions when user checked *Don't ask again*

So, You will be taken to the application settings screen.
```Kotlin
            .whenDenied { deniedPermissions, foreverDeniedPermissions ->
		if(foreverDeniedPermissions.isNotEmpty(){
			PermissionManager.openApplicationSettings(this, application.packageName)
		}
            }
```

## License
```
The MIT License (MIT)

Copyright (c) 2019 Kyeonghwan-Kong

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
