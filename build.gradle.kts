plugins {
    kotlin("jvm") version "1.9.23"
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.2.1"
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
    implementation("guru.nidi:graphviz-java:0.18.1")
    implementation("org.graalvm.js:js:20.0.0")
}

group = "io.github.terrakok"
version = "1.1"

gradlePlugin {
    website = "https://github.com/terrakok/kmp-hierarchy"
    vcsUrl = "https://github.com/terrakok/kmp-hierarchy"
    plugins {
        create("KmpHierarchyPlugin") {
            id = "io.github.terrakok.kmp-hierarchy"
            displayName = "KMP hierarchy plugin"
            description = "Simple gradle plugin for printing KMP source sets hierarchy."
            tags = listOf("kotlin", "kmp", "multiplatform", "debug", "build")
            implementationClass = "io.github.terrakok.KmpHierarchyPlugin"
        }
    }
}