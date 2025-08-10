# Domain module consumer rules
# These rules will be applied to consumers of this module

# Keep all domain entities and interfaces
-keep class com.sample.android.domain.entity.** { *; }
-keep interface com.sample.android.domain.repository.** { *; }
-keep class com.sample.android.domain.usecase.** { *; }