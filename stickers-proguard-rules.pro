-optimizationpasses 5
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keepparameternames

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent
-keep public class * extends android.preference.Preference


-keep public class * extends android.support.v7.app.ActionBarActivity
-keep public class * extends android.support.v7.app.AppCompatActivity
-keep public class * extends android.support.v4.app.Fragment


-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclassmembers class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}


# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ------------------ SUPPORT
-dontwarn android.support.**

-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }


#---------------- OkHTTP
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }

#------------------- GSON
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

#------------------- Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

#------------------- Rx
-dontwarn rx.internal.util.unsafe.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}

#------------------- EventBus
-keepclassmembers class ** {
    public void onEvent(**);
}
-keepclassmembers class ** {
    public void onEventMainThread(**);
}

#------------------- support design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

# ------------------ STICKER FACTORY
-dontwarn java.lang.invoke.*
-keepclassmembers class vc908.stickerfactory.** {
    <fields>;
}
-keepattributes InnerClasses
-keepattributes EnclosingMethod

-keep public class vc908.stickerfactory.StickersManager{*;}

-keep public interface vc908.stickerfactory.ui.OnStickerSelectedListener{*;}
-keep public interface vc908.stickerfactory.ui.OnEmojiBackspaceClickListener{*;}
-keep public interface vc908.stickerfactory.ui.OnStickerFileSelectedListener{*;}
-keep public interface vc908.stickerfactory.ui.OnShopButtonClickedListener{*;}

-keep public class vc908.stickerfactory.EmojiSettingsBuilder{*;}
-keep public class vc908.stickerfactory.EmojiSettingsBuilder$EmojiResourceLocation{*;}

-keep public class vc908.stickerfactory.ui.fragment.StickersFragment{*;}
-keep public class vc908.stickerfactory.ui.dialog.DialogManager{*;}

-keep public class vc908.stickerfactory.ui.view.BlockingListView{*;}
-keep public class vc908.stickerfactory.ui.view.BadgedStickersButton{*;}
-keep public class vc908.stickerfactory.ui.view.StickersKeyboardLayout{*;}
-keep public interface vc908.stickerfactory.SubscriptionListener{*;}

-keep public interface vc908.stickerfactory.NetworkService{*;}

-keep public class vc908.stickerfactory.StickersKeyboardController{*;}
-keep public class vc908.stickerfactory.StickersKeyboardController$Builder { *; }
-keep public interface vc908.stickerfactory.StickersKeyboardController$KeyboardVisibilityChangeListener{*;}
-keep public interface vc908.stickerfactory.StickersKeyboardController$KeyboardVisibilityChangeIntentListener{*;}

-keep public class vc908.stickerfactory.StickerLoader{*;}
-keep public class vc908.stickerfactory.ManagerFacade{*;}

-keep public class vc908.stickerfactory.SplitManager{*;}
-keep public class vc908.stickerfactory.NetworkManager{*;}
-keep public class vc908.stickerfactory.StorageManager{*;}
-keep public interface vc908.stickerfactory.analytics.IAnalytics{*;}
-keep public class vc908.stickerfactory.ui.fragment.StickersListFragment{*;}

-keep public class vc908.stickerfactory.emoji.Emoji{*;}
-keep public class vc908.stickerfactory.utils.Utils{*;}
-keep public class vc908.stickerfactory.utils.NamesHelper{*;}
-keep public class vc908.stickerfactory.utils.CompatUtils{*;}
-keep public class vc908.stickerfactory.utils.KeyboardUtils{*;}

-keep public class vc908.stickerfactory.billing.PricePoint{*;}
-keep public class vc908.stickerfactory.billing.Prices{*;}

-keep class vc908.stickerfactory.model.** { *; }
-keep class vc908.stickerfactory.events.** { *; }

-keep class vc908.stickerfactory.User { *; }

-keep public class vc908.stickerfactory.StorageManager {
    public boolean isGcmTokenSent();
    public void storeIsGcmTokenSent(boolean);
    public static ** getInstance();
}

-keep public class vc908.stickerfactory.analytics.AnalyticsManager {
    public void onUserMessageSent(boolean);
    public static ** getInstance();
}

-keep public class vc908.stickerfactory.ui.activity.ShopWebViewActivity{
    protected <methods>;
}

-keep public class vc908.stickerfactory.ui.activity.ShopWebViewActivity$AndroidJsInterface
-keep public class * implements vc908.stickerfactory.ui.activity.ShopWebViewActivity$AndroidJsInterface
-keepclassmembers class vc908.stickerfactory.ui.activity.ShopWebViewActivity$AndroidJsInterface {
    <methods>;
}

-keepattributes JavascriptInterface

-dontwarn com.google.android.gms.**