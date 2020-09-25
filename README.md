# Mosaic

[Jetpack Compose](https://developer.android.com/jetpack/compose) for console UI. Inspired by [Ink](https://github.com/vadimdemedes/ink).


## Usage

```kotlin
fun main() = runMosaic {
  setContent {
    Text("Hello, World!")
  }
}
```

Run work inside the `runMosaic` block after calling `setContent`. The output will automatically
update as you alter shared state.


## Examples

Run `./gradlew installDist` to build the example binaries.

 * Counter: A simple increasing number from 0 until 20.

   `./examples/counter/build/install/counter/bin/counter`

 * Jest: Example output of a test framework (such as JS's 'Jest').

   `./examples/jest/build/install/jest/bin/jest`


# License

    Copyright 2020 Jake Wharton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
