buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
<<<<<<< HEAD
    id("com.android.application") version "8.2.1" apply false
=======
    id("com.android.application") version "8.9.0" apply false
>>>>>>> origin/master
    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.2" apply false

}