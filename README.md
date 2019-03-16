# test-accessors
#### A codegen utility to generate test-only accessors for otherwise unaccessible fields in your classes
## Usage
Add the dependencies to your project:
```groovy
repositories {
  maven {
    url "https://jitpack.io"
  }
}
dependencies {
    // 1. Add the annotations to the sourceset you want them in
    // For example, for your unit tests
    testCompileOnly "com.github.stoyicker.test-accessors:annotations:<version>"
    // Or for Android instrumented tests
    androidTestCompileOnly "com.github.stoyicker.test-accessors:annotations:<version>"
    // 2. Add the processor you want to use to each of the classpaths corresponding to those you put the annotations in
    // If you use Java
    testApt "com.github.stoyicker.test-accessors:processor-java:<version>"
    androidTestApt "com.github.stoyicker.test-accessors:processor-java:<version>"
    // If you use Kotlin
    testApt "com.github.stoyicker.test-accessors:processor-kotlin:<version>"
    androidTestApt "com.github.stoyicker.test-accessors:processor-kotlin:<version>"
}
```
Annotate your field:
```java
package org.my.example;

public class MyClass {
    @RequiresAccessor
    private final String myField;
}
```
Once annotation processing runs, there will be a class called org.my.example.GeneratedMyClassAccesors in the generated 
directory of your specified source set with two methods with the following signature:
```java
// Getter
public String myField(MyClass instance);

// Setter
public void myField(MyClass instance, String newValue);
```
If you are using Kotlin, you can take advantage of the Kotlin artifact instead for a more idiomatic usage via extension
functions. For example,
```kotlin
package org.my.example

class MyClass {
    @RequiresAccessor
    private val String = "hola"
}
```
will generate the following extension methods for MyClass in the specified source set:
```kotlin
fun MyClass.myField(): String

fun MyClass.myField(newValue: String): Unit
```
## Options
#### Annotation
The annotation has some parameters you can use to alter its behavior:
* name -> Allows you to change the name of the methods that will be generated for the field you are annotating. If 
unspecified, the name of the field will be used.
* requires -> Allows you to specify which type of accessor you want (use AccessorType.TYPE_GETTER for getter and/or
AccessorType.TYPE_SETTER for setter) for your annotated field. By default, both getter and setter will be generated.
#### Processor
You can pass the following processor-wide options as compiler arguments:
* testaccessors.requiredPatternInClasspath -> Allows you to specify a regex that classes in your classpath will be 
checked against every time an accessor is invoked. This allows you to ensure that accessors are not used in tests by 
using a regex that matches packages that are found in test-specific dependencies. By default, this uses a regex that 
matches both TestNG and JUnit.
* testaccessors.logLevel -> Allows you to specify the verbosity of the tool with regards to potential misuses of it. 
Needs to be one of "nothing", "errors", "warnings" and "all". The default and fallback if malformed is "all".
## Disclaimer: when should I use this?
When dealing with code that is to be tested, you normally want to write it in a way that respects certain good 
principles to allow and facilitate testing it. However, due to circumstances of life, such as legacy code, 
API requirements, primitive types and whatnot, sometimes this is just not possible - at least not without sacrificing 
the quality of the code or refactorings that are too costly.
I have written this tool for situations like these. However, bear in mind that abusing this tool will slow down your 
tests as the generated code uses the Java reflection API underneath (and just like it, it is a tool that exists because 
it has its use cases, but you should normally avoid it if at all possible).
