# test-accessors
### An annotation processor that generates code for your tests to be able to access and modify private/final fields so you don't have to use anti-patterns such as @VisibleForTesting!
[![CircleCI](https://circleci.com/gh/stoyicker/test-accessors.svg?style=svg)](https://circleci.com/gh/stoyicker/test-accessors)
## Usage
[ ![Download](https://api.bintray.com/packages/stoyicker/test-accessors/annotations/images/download.svg) ](https://search.maven.org/search?q=g:com.github.stoyicker.test-accessors)

Add the dependencies to your project:
```groovy
repositories {
  google()
  mavenCentral()
}
dependencies {
    // 1. Add the annotation
    implementation "com.github.stoyicker.test-accessors:annotations:<version>"
    // 2. Add the processor
    annotationProcessor "com.github.stoyicker.test-accessors:processor-java:<version>"
}
```
Annotate your field:
```java
public final class MyClass {
    @RequiresAccessor(requires = {RequiresAccessor.AccessorType.TYPE_GETTER, RequiresAccessor.AccessorType.TYPE_SETTER})
    private final String myField = "hola hola";
}
```
Once annotation processing runs, there will be a class in the generated directory of your source set
with two methods like this:
```java
public final class MyClassTestAccessors {
    public static <T> T myField(final MyClass receiver);
    
    public static <T> void myField(final MyClass receiver, final T newValue);
}
```
As you can see, things work perfectly fine even with final fields. Moreover, it also works with static fields!
```java
class MyClass {
    @RequiresAccessor(requires = {RequiresAccessor.AccessorType.TYPE_GETTER, RequiresAccessor.AccessorType.TYPE_SETTER})
    private static final String myStaticField = "static hola hola";
}
```
will generate an implementation under the following API in the current source set:
```java
public final class MyClassTestAccessors {
    public static <T> T myStaticField();
    
    public static <T> void myStaticField(final T newValue);
}
```
The different sample projects within the repo showcase how to use all of the possibilities that the
processor offers, so check them out if you're feeling lost!
## Options
### Annotation level
The annotation has some parameters you can use to alter its behavior:
* name -> Allows you to change the name of the methods that will be generated for the field you are 
annotating. If unspecified, the name of the field will be used.
* requires -> Allows you to specify which type of accessor you want (use AccessorType.TYPE_GETTER 
for getter and/ or AccessorType.TYPE_SETTER for setter) for your annotated field. If unspecified, 
only a setter will be generated.
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
* testaccessors.requiredClasses -> Allows you to specify a list of comma-separated class names that
will be 'pinged' every time a generated method runs, triggering an exception if none of them are
found. This allows you to ensure that generated methods are not used where they should not (such as 
outside of tests) by passing in classes that are specific to test artifacts. If unspecified, it 
becomes a list of junit.runner.BaseTestRunner (for JUnit 4), org.junit.jupiter.api.Test (for JUnit 5)
and org.testng.TestNG (for TestNG).
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
Non-Android Java:
```groovy
compileJava {
    options.compilerArgs.addAll(['-Atestaccessors.requiredClasses=yourFirstClass,yourSecondClass'])
}
```
Android:
```groovy
android {
  defaultConfig {
    javaCompileOptions {
      annotationProcessorOptions {
        arguments = ['testaccessors.requiredClasses': 'yourFirstClass,yourSecondClass']
      }
    }
  }
}
```
## Drawbacks?
Classpath bloat: If you don't use the generated methods outside of tests, a simple shrinking ProGuard configuration 
such as [this one](sample-android/proguard/rules.pro) will make sure that your classpath does not 
get affected at all.
Additionally, all accessors are generated in the last round of annotation processing, which means 
they will not be considered by other annotation processors and therefore won't slow down their
executions.
