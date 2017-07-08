# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Effiong\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:
#-keep class javax.** { *; }
#-keep class org.** { *; }
-keep class com.afollestad.** { *; }
-keep class android.support.v7.widget.** { *; }
-keep class android.support.design.widget.** { *; }
-keep class com.simplecityapps.recyclerview_fastscroll.** { *; }
-keepattributes InnerClasses
-dontoptimize
-dontobfuscate
#-keepattributes InnerClasses
#-dontoptimize
#-dontusemixedcaseclassnames
#-dontskipnonpubliclibraryclasses
#-verbose
#-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
#-dontwarn com.google.android.gms.**
# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
