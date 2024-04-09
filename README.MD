# KMP-hierarchy
### Gradle Plugin to investigate KMP project's hierarchy

--------------
⚠️⚠️⚠️ The plugin is penting an approval on the gradle plugins repository ⚠️⚠️⚠️

--------------
This open-source Gradle plugin aims to help developers investigate 
the HMPP structure of Kotlin Multiplatform (KMP) source sets with ease.
By graphically visualizing the relationships between source sets and clearly indicating their hierarchy, 
the plugin simplifies understanding and maintaining complex KMP projects.

--------------
## Features
- Graphical visualization of dependencies between source sets
- Seamless Integration with Gradle builds
- Full compatibility with any KMP project
- Various output formats (SVG, PNG, JSON, DOT etc)

--------------
## Installation
To use KMP-hierarchy plugin, include the following in your build.gradle(.kts) file:
```kotlin
plugins {
    id("io.terrakok.github.kmp-hierarchy") version "1.0"
}
```

--------------
## Usage
After successful installation, the plugin will be ready to use.
```text
./gradlew :printHierarchy
```
Example of SVG output for [compose-multiplatform-core/compose/ui/ui](https://github.com/JetBrains/compose-multiplatform-core/tree/jb-main/compose/ui/ui) module:
![](https://raw.githubusercontent.com/terrakok/kmp-hierarchy/master/media/compose-ui-ui.svg)

--------------
## Configuration

```kotlin
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("io.terrakok.github.kmp-hierarchy")
}

kotlin {
    //...
    printHierarchy {
        //Sets the formats to be used for generating output files.
        formats(Format.SVG)

        //Boolean variable indicating whether to include test hierarchy in the generated output.
        withTestHierarchy = false

        //The output directory where the generated files will be saved.
        outputDir.set(layout.buildDirectory.dir("kmp-hierarchy"))
    }
}
```
