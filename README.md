# test-accessors
### An annotation processor that generates code for your tests to be able to access and modify private/final fields so you don't have to use anti-patterns such as @VisibleForTesting!
[![CircleCI](https://circleci.com/gh/stoyicker/test-accessors.svg?style=svg)](https://circleci.com/gh/stoyicker/test-accessors)
## Usage
Add the dependencies to your project:
```groovy
repositories {
  google()
  jcenter()
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
Once annotation processing runs, there will be a class called org.my.example.GeneratedMyClassAccessors 
in the generated directory of your source set with two methods with the following signature:
```java
public final class MyClassTestAccessors {
    public static <TestAccessorsValue> TestAccessorsValue myField(final MyClass receiver);
    
    public static <TestAccessorsValue> void myField(final MyClass receiver, final TestAccessorsValue newValue);
}
```
It also works with static variables!
```java
class MyClass {
    @RequiresAccessor
    private static String myStaticField = "static hola hola";
}
```
will generate an implementation under the following API in the current source set:
```java
public final class MyClassTestAccessors {
    public static <TestAccessorsValue> TestAccessorsValue myStaticField();
    
    public static <TestAccessorsValue> void myStaticField(final TestAccessorsValue newValue);
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
compileJava {
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
## Requirements
* Gradle 5.2 or above.
* (Recommended) IntelliJ IDEA 2019.1/Android Studio 3.5 or above as it fixes some issues with the 
generated code not being visible to the IDE, although CMD-based compilation works just fine.
