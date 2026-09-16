-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keep class androidx.health.connect.client.** { *; }
-keep interface androidx.health.connect.client.** { *; }
-keep class androidx.health.connect.client.records.** { *; }
-keep class androidx.health.connect.client.units.** { *; }
-keep class androidx.health.connect.client.request.** { *; }
-keep class androidx.health.connect.client.response.** { *; }
-keep class androidx.health.connect.client.aggregate.** { *; }
-keep class androidx.health.connect.client.time.** { *; }

-keep class androidx.datastore.preferences.protobuf.** { *; }

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keep class androidx.work.** { *; }
-keep class androidx.startup.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

-keep class com.haruma.health.kit.data.model.** { *; }
-keep class com.haruma.health.kit.di.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keep class * extends androidx.lifecycle.ViewModel