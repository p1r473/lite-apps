import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
  extra["kotlin_version"] = "1.3.61"

  repositories {
    jcenter()
  }

  dependencies {
    classpath(kotlin("gradle-plugin", version = rootProject.extra["kotlin_version"] as String?))
    // NOTE: Do not place your application dependencies here; they belong
    // in the individual module build.gradle files
  }
}

allprojects {
  repositories {
    jcenter()
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "1.8"
  }
}
