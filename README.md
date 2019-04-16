# test-accessors
### An annotation processor that generates code for your tests to be able to access and modify private/final fields so you don't have to use anti-patterns such as @VisibleForTesting!
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
    annotationProcessor "com.github.stoyicker.test-accessors:processor-java:<version>"
    // If on Kotlin (or mixed Java/Kotlin)
    kapt "com.github.stoyicker.test-accessors:processor-kotlin:<version>"
    // In the case of mixed projects, you'll be able to use the generated code from both Kotlin and Java just fine,
    // so choose whichever one you like the most
}
```
Annotate your field:
```java
public final class MyJavaClass {
    @RequiresAccessor
    private final String myField;
}
```
Once annotation processing runs, there will be a class called org.my.example.GeneratedMyClassAccessors in the generated 
directory of your source set with two methods with the following signature:
```java
public final class MyJavaClass {
    public static String myField(final MyClass receiver);
    
    public static void myField(final MyClass receiver, final String newValue);
}
```
If you are using Kotlin, you can take advantage of the Kotlin artifact instead for a more idiomatic usage via extension
functions. For example,
```kotlin
class MyKotlinClass {
    @RequiresAccessor
    private val myField = "hola"
}
```
will generate an implementation under the following API in the current source set:
```kotlin
@file:JvmName("MyKotlinClassTestAccessors")

fun MyKotlinClass.myField(): String

fun MyKotlinClass.myField(newValue: String): Unit
```
## Options
### Annotation level
The annotation has some parameters you can use to alter its behavior:
* name -> Allows you to change the name of the methods that will be generated for the field you are 
annotating. If unspecified, the name of the field will be used.
* requires -> Allows you to specify which type of accessor you want (use AccessorType.TYPE_GETTER 
for getter and/ orAccessorType.TYPE_SETTER for setter) for your annotated field. If unspecified, 
both getter and setter will be generated.
* androidXRestrictTo -> Allows you to declare an instance of androidx.annotation.RestrictTo that 
will be added to the method(s) generated due to this annotation. If unspecified or the scope array 
that describes the restrictions is empty, no androidX RestrictTo annotation will be added to methods 
generated due to this annotation occurrence (unless overriden by 
testaccessors.defaultAndroidXRestrictTo, see below).
* supportRestrictTo -> Allows you to declare an instance of android.support.annotation.RestrictTo 
that will be added to the method(s) generated due to this annotation. If unspecified or the scope 
array that describes the restrictions is empty, no support RestrictTo annotation will be added to 
methods generated due to this annotation occurrence (unless overriden by
testaccessors.defaultSupportRestrictTo, see below).
### Processor level
* testaccessors.requiredPatternInClasspath -> Allows you to specify a regex that artifact names in 
the corresponding classpath will be checked against every time a generated method runs. This allows 
you to ensure that generated methods are not used where they should not (such as outside of tests) 
by causing an unchecked exception to be thrown at runtime if the regex does not match at all. If 
unspecified or invalid, it becomes a regex that matches both TestNG and JUnit.
* testaccessors.defaultAndroidXRestrictTo -> Allows you to specify a default 
androidx.annotation.RestrictTo scope that will cause all occurrences of RequiresAccessors to change 
their default value for androidXRestrictTo to an instance of RestrictTo with that scope. Must be a
comma-separated String formed by one or more of "LIBRARY", "LIBRARY_GROUP", "GROUP_ID", "TESTS" and 
"SUBCLASSES".
* testaccessors.defaultSupportRestrictTo -> Allows you to specify a default 
android.support.annotation.RestrictTo scope that will cause all occurrences of RequiresAccessors to 
change their default value for supportRestrictTo to an instance of RestrictTo with that scope. Must 
be a comma-separated String formed by one or more of "LIBRARY", "LIBRARY_GROUP", "GROUP_ID", "TESTS"
and "SUBCLASSES".
##### How do I pass arguments to the annotation processor?
Frameworkless Java:
```groovy
compileJava{
    options.compilerArgs.addAll(['-Atestaccessors.requiredPatternInClasspath=yourRegex'])
}
```
Android with Java:
```groovy
android {
  defaultConfig {
    javaCompileOptions {
      annotationProcessorOptions {
        arguments = ['testaccessors.requiredPatternInClasspath': 'yourRegex']
      }
    }
  }
}
```
Kotlin:
```groovy
kapt {
    arguments {
        arg('testaccessors.requiredPatternInClasspath', 'yourRegex')
    }
}
```
## How does my classpath get affected?
If you don't use the generated methods outside of tests, a simple shrinking ProGuard configuration 
such as [this one](sample-android/proguard/rules.pro) will make sure that your classpath does not 
get affected at all.
Additionally, the accessors are generated in the last round of annotation processing, which means 
they will not be considered by other annotation processors and therefore won't impact their 
executions.
## Caveats
* My generated Java code is not visible to Gradle via CMD -> Before Gradle 5.2, Java 
annotation processing wasn't exactly supported, so you'll either need to upgrade your Gradle version 
or use some other workaround. For more information and possible solutions, please check 
[gradle/gradle#2300][gradle java apt issues]. Note that some plugins that you may be using (such as 
the Android plugin) take care of this by themselves, so check your setup for issues before trying to 
fix a problem you may not have!
* My generated Java code is not visible to my IDE -> Check [gradle/gradle#2300][gradle java apt issues]
for information and solutions for different IDEs.
## License
https://creativecommons.org/licenses/by/4.0/legalcode

[gradle java apt issues]: https://github.com/gradle/gradle/issues/2300
