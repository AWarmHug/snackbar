# snackbar
<img src="https://raw.githubusercontent.com/oginotihiro/snackbar/master/screenshots/sample.gif" width="240" />

## Change

增加系统默认样式，只需要修改包名就可以

## Usage

### 1. Add Snackbar to your project

Add this in your build.gradle file

```gradle
compile 'com.oginotihiro:snackbar:1.0.0'
```

### 2. Show a message
```java
Snackbar.make(SnackbarCustomLayout, ViewToFindParent, Snackbar.BOTTOM_TOP, Snackbar.LENGTH_SHORT).show();
```

## Compatibility

Supported on API level 10 and above (2.3+)

## License

    Copyright 2016 oginotihiro

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
