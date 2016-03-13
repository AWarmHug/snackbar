# snackbar
![snackbar](screenshots/sample.gif) 

Supported on API Level 8 and above

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
