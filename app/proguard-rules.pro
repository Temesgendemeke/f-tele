# ProGuard / R8 rules for F-Tele native build
-repackageclasses ''
-allowaccessmodification

# Service & Activity declared in AndroidManifest
-keep class com.example.ftele.service.SmsMuterService { <init>(...); }
-keep class com.example.ftele.ui.MainActivity { <init>(...); }

