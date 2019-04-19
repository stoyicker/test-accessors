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
    kapt "com.github.stoyicker.test-accessors:processor-java:<version>"
}
```
Annotate your field:
```java
public final class MyClass {
    @RequiresAccessor
    private final String myField = "hola hola";
}
```
Once annotation processing runs, there will be a class called org.my.example.GeneratedMyClassAccessors in the generated 
directory of your source set with two methods with the following signature:
```java
public final class MyClassTestAccessors {
    public static String myField(final MyClass receiver);
    
    public static void myField(final MyClass receiver, final String newValue);
}
```
It also works with static variables!
```kotlin
class MyClass {
    @RequiresAccessor
    private static String myStaticField = "static hola hola";
}
```
will generate an implementation under the following API in the current source set:
```java
public final class MyClassTestAccessors {
    public static String myStaticField();
    
    public static void myStaticField(final String newValue);
}
```
If you like to live on the edge, there is an alternate processor in development which generates 
Kotlin code instead of Java. This code is a bit more idiomatic as the generated methods are extensions 
on the type of the subject, which reduces the amount of parameters to 1 in setters for members variables 
and none in getters for members fields, plus aims to respect compile-time nullability constraints 
wherever possible. It is otherwise similar to the Java one, and it can be used from both Kotlin and
Java. However, be aware that it is still in development and will likely break if you use it to parse
annotation on parameterized and/or nullable fields. To use it, replace the processor dependency in 
your Gradle file:
```groovy
dependencies {
    // If on Kotlin (or mixed Java/Kotlin)
    kapt "com.github.stoyicker.test-accessors:processor-java:<version>" -> kapt "com.github.stoyicker.test-accessors:processor-kotlin:<version>"
}
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
* Why are setters not supported for static final fields? -> Acceses to these fields are inlined at compile time. Long story short, while the field itself can be updated via reflection, since its direct accessors will still use the original value, there's not much reason why you would want to set it. But if you feel like there's a valid scenario to consider revisiting this decision, please feel free to [open an issue!](https://github.com/stoyicker/test-accessors/issues/new) 
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
