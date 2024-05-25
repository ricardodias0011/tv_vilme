-keep class java.sql.Timestamp {
    *;
}

-keep class java.time.** {
    *;
}

-keep class com.google.android.exoplayer2.** { *; }
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }

-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.Timestamp { *; }
-keep class com.nest.nestplay.model.UserModel { *; }
-keep class com.nest.nestplay.model.FavsMovie { *; }
-keep class com.nest.nestplay.model.ListEpisodesModel { *; }
-keep class com.nest.nestplay.model.ChannelTVModel { *; }
-keep class com.nest.nestplay.model.MovieModel { *; }
-keep class com.nest.nestplay.model.TimeModel { *; }
-keep class com.nest.nestplay.model.ListMovieModel { *; }
-keep class com.nest.nestplay.model.ListMovieModel$Movie { *; }
-keep class com.nest.nestplay.model.LinksTvModel { *; }
-keep class com.nest.nestplay.model.ListTvModel { *; }
-keep class com.nest.nestplay.model.ListChannelTVModel { *; }

-dontwarn afu.org.checkerframework.dataflow.qual.Pure
-dontwarn afu.org.checkerframework.dataflow.qual.SideEffectFree
-dontwarn afu.org.checkerframework.framework.qual.EnsuresQualifierIf
-dontwarn afu.org.checkerframework.framework.qual.EnsuresQualifiersIf
-dontwarn com.google.firebase.crashlytics.buildtools.reloc.afu.org.checkerframework.checker.formatter.qual.ConversionCategory
-dontwarn com.google.firebase.crashlytics.buildtools.reloc.afu.org.checkerframework.checker.formatter.qual.ReturnsFormat
-dontwarn com.google.firebase.crashlytics.buildtools.reloc.afu.org.checkerframework.checker.nullness.qual.EnsuresNonNull
-dontwarn com.google.firebase.crashlytics.buildtools.reloc.afu.org.checkerframework.checker.regex.qual.Regex
-dontwarn com.google.firebase.crashlytics.buildtools.reloc.org.checkerframework.checker.formatter.qual.ConversionCategory
-dontwarn com.google.firebase.crashlytics.buildtools.reloc.org.checkerframework.checker.formatter.qual.ReturnsFormat
-dontwarn com.google.firebase.crashlytics.buildtools.reloc.org.checkerframework.checker.nullness.qual.EnsuresNonNull
-dontwarn com.google.firebase.crashlytics.buildtools.reloc.org.checkerframework.checker.regex.qual.Regex
-dontwarn com.squareup.okhttp.CipherSuite
-dontwarn com.squareup.okhttp.ConnectionSpec
-dontwarn com.squareup.okhttp.TlsVersion
-dontwarn java.lang.reflect.AnnotatedType
-dontwarn javax.servlet.ServletContextEvent
-dontwarn javax.servlet.ServletContextListener
-dontwarn org.ietf.jgss.GSSContext
-dontwarn org.ietf.jgss.GSSCredential
-dontwarn org.ietf.jgss.GSSException
-dontwarn org.ietf.jgss.GSSManager
-dontwarn org.ietf.jgss.GSSName
-dontwarn org.ietf.jgss.Oid
