## Classroom Forum - Firebase

#### To set up project:
1. Go to Firebase, create new project and link Firebase to Android Studio using package name.
2. Download and insert 'google-services.json' from Firebase to app directory.
3. Import all dependancies:
+ build.gradle (project level) —> id("com.google.gms.google-services") version "4.4.1" apply false
+ build.gradle (app level):
+   plugin —> id("com.google.gms.google-services")
+   dependancies —>
++     plementation(platform("com.google.firebase:firebase-bom:32.8.0"))
++     implementation("com.google.firebase:firebase-firestore")
++     implementation("com.google.firebase:firebase-auth")
++     implementation("com.google.firebase:firebase-analytics")
