// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("io.github.cdimascio:java-dotenv:5.2.2")
    }
}

plugins {
    id("com.android.application") version "8.8.0" apply false
    id("com.android.library") version "8.8.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.ncorti.ktfmt.gradle") version "0.21.0"
    id("org.owasp.dependencycheck") version "8.2.1"
}

ktfmt {
    kotlinLangStyle()
}