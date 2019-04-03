# test-accessors
### A codegen utility to generate test-only accessors for otherwise unaccessible fields in your Java classes.
## Usage
Add the dependencies to your project:
```groovy
repositories {
  maven {
    url "https://jitpack.io"
  }
}
dependencies {
    // 1. Add the annotation
    implementation "com.github.stoyicker.test-accessors:annotations:<version>"
    // 2. Add the processor you want
    // If on Java
    apt "com.github.stoyicker.test-accessors:processor-java:<version>"
    // If on Kotlin (or mixed Java/Kotlin)
    kapt "com.github.stoyicker.test-accessors:processor-kotlin:<version>"
    // In the case of mixed projects, you'll be able to use the generated code from both Kotlin and Java just fine,
    // so choose whichever one you like the most
}
```
Annotate your field:
```java
package org.my.example;

public final class MyJavaClass {
    @RequiresAccessor
    private final String myField;
}
```
Once annotation processing runs, there will be a class called org.my.example.GeneratedMyClassAccessors in the generated 
directory of your source set with two methods with the following signature:
```java
public final class MyJavaClass {
    // Getter
    public static <T> T myField(final MyClass instance);
    
    // Setter
    public static void myField(final MyClass instance, final Object newValue);
}
```
If you are using Kotlin, you can take advantage of the Kotlin artifact instead for a more idiomatic usage via extension
functions. For example,
```kotlin
package org.my.example

class MyKotlinClass {
    @RequiresAccessor
    private val myField = "hola"
}
```
will generate an implementation under the following API in the current source set:
```kotlin
@Generated
object MyKotlinClassTestAccessors {
  @JvmStatic
  fun <T> MyClass.myField(): T

  @JvmStatic
  fun <T> MyClass.myField(newValue: Any?): Unit
}
```
## Options
#### Annotation
The annotation has some parameters you can use to alter its behavior:
* name -> Allows you to change the name of the methods that will be generated for the field you are annotating. If 
unspecified, the name of the field will be used.
* requires -> Allows you to specify which type of accessor you want (use AccessorType.TYPE_GETTER for getter and/or
AccessorType.TYPE_SETTER for setter) for your annotated field. By default, both getter and setter will be generated.
## Disclaimer: when should I use this?
When dealing with code that is to be tested, you normally want to write it in a way that respects certain good 
principles to allow and facilitate testing it. However, due to circumstances of life, such as legacy code, 
API requirements, primitive types and whatnot, sometimes this is just not possible - at least not without sacrificing 
the quality of the code or refactorings that are too costly.
I have written this tool for situations like these. However, bear in mind that abusing this tool will slow down your 
tests as the generated code uses the Java reflection API underneath (and just like it, it is a tool that exists because 
it has its use cases, but you should normally avoid it if at all possible).
## License
https://creativecommons.org/licenses/by/4.0/legalcode
