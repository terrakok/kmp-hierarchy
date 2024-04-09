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

group = "io.terrakok.github"
version = "1.0"

gradlePlugin {
    plugins {
        create("KmpHierarchyPlugin") {
            id = "io.terrakok.github.kmp-hierarchy"
            implementationClass = "io.terrakok.github.KmpHierarchyPlugin"
        }
    }
}