#输出完整构建报告
-printconfiguration full-r8-config.txt

-dontwarn
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose

#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-dontoptimize

-keep class com.jlm.common.compat.BaseActCompat { *; }
-keep class * extends com.jlm.common.compat.BaseActCompat


-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference



-dontwarn org.xmlpull.v1.**
-dontnote org.xmlpull.v1.**
-keep class org.xmlpull.** { *; }
-keepclassmembers class org.xmlpull.** { *; }
-dontwarn org.kxml2.io.**
-dontwarn android.content.res.**
-dontwarn org.slf4j.impl.StaticLoggerBinder

## 保留所有 Parcelable 实现类的特殊属性.
-keepclassmembers class * implements android.os.Parcelable {
     static android.os.Parcelable$Creator CREATOR;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
## TheRouter
# 如果使用了 Fragment 路由，需要保证类名不被混淆
# -keep public class * extends android.app.Fragment
# -keep public class * extends androidx.fragment.app.Fragment
# -keep public class * extends android.support.v4.app.Fragment

-keep class androidx.annotation.Keep
-keep @androidx.annotation.Keep class * {*;}
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}
-keepclasseswithmembers class * {
    @com.therouter.router.Autowired <fields>;
}

## ShapeView
-keep class com.hjq.shape.** {*;}

## Bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

## x5webview
-dontwarn dalvik.**
-dontwarn com.tencent.smtt.**
-keep class com.tencent.smtt.** {
    *;
}
-keep class com.tencent.tbs.** {
    *;
}

## Retrofit   新版AS会强制开启R8混淆全模式，  Retrofit 2.9.0需要添加以下混淆规则
# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

## PictureSelector
-keep class com.luck.picture.lib.** { *; }
# 如果引入了Camerax库请添加混淆
-keep class com.luck.lib.camerax.** { *; }
# 如果引入了Ucrop库请添加混淆
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

#kotlin 反射
-keepattributes *Annotation*
-keep class kotlin.** { *; }
-keep class org.jetbrains.** { *; }


#libpag
-keep class org.libpag.** {*;}
-keep class androidx.exifinterface.** {*;}

#nui
-keep class com.alibaba.idst.nui.*{*;}

#友盟
-keep class com.umeng.** { *; }

-keep class com.uc.** { *; }

-keep class com.efs.** { *; }

-keepclassmembers class *{
     public<init>(org.json.JSONObject);
}
-keepclassmembers enum *{
      publicstatic**[] values();
      publicstatic** valueOf(java.lang.String);
}

-keep class org.repackage.** {*;}

-keep class com.uyumao.** { *; }

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

## ================================ MiniMax 语音合成相关混淆规则 ================================

## Gson 混淆规则
-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep,allowobfuscation @interface com.google.gson.annotations.SerializedName

## kotlinx.serialization 混淆规则
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have problems with serialization.
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Change here com.yourcompany.yourpackage
-keep,includedescriptorclasses class com.jlm.translator.intelligent.speech.miniMax.model.**$$serializer { *; }
-keepclassmembers class com.jlm.translator.intelligent.speech.miniMax.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.jlm.translator.intelligent.speech.miniMax.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

## MiniMax 数据模型类保护 - 保持所有字段名不被混淆
-keep class com.jlm.translator.intelligent.speech.miniMax.model.TaskStartDto { *; }
-keep class com.jlm.translator.intelligent.speech.miniMax.model.TaskStartDto$* { *; }
-keep class com.jlm.translator.intelligent.speech.miniMax.model.TaskContinueDto { *; }
-keep class com.jlm.translator.intelligent.speech.miniMax.model.TaskFinishDto { *; }
-keep class com.jlm.translator.intelligent.speech.miniMax.model.TaskConnectResp { *; }
-keep class com.jlm.translator.intelligent.speech.miniMax.model.TaskConnectResp$* { *; }

## WebSocket 相关保护
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okio.** { *; }

## 保护 WebSocketListener 的回调方法
-keep class * extends okhttp3.WebSocketListener {
    public void onOpen(okhttp3.WebSocket, okhttp3.Response);
    public void onMessage(okhttp3.WebSocket, java.lang.String);
    public void onMessage(okhttp3.WebSocket, okio.ByteString);
    public void onClosing(okhttp3.WebSocket, int, java.lang.String);
    public void onClosed(okhttp3.WebSocket, int, java.lang.String);
    public void onFailure(okhttp3.WebSocket, java.lang.Throwable, okhttp3.Response);
}

## MiniMax 语音合成服务类保护
-keep class com.jlm.translator.intelligent.speech.miniMax.MinimaxSynthHelper { *; }
-keep class com.jlm.translator.intelligent.speech.miniMax.MinimaxSynthHelper$* { *; }

## 保护所有带有 @Serializable 注解的类
-keep @kotlinx.serialization.Serializable class * { *; }
-keep class **$$serializer { *; }

## 保护 BouncyCastle Hex 解码相关
-dontwarn org.bouncycastle.**
-keep class org.bouncycastle.util.encoders.Hex { *; }

## 保护音频播放相关类
-keep class com.jlm.translator.intelligent.speech.AudioPlayer { *; }
-keep class com.jlm.translator.intelligent.speech.AudioPlayer$* { *; }