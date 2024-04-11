package io.terrakok.github

import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.model.Factory.graph
import guru.nidi.graphviz.model.Factory.node
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import java.io.File
import java.io.Serializable
import javax.inject.Inject

class KmpHierarchyPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val project = target
        project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            val kotlinExt = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

            val configDsl = (kotlinExt as ExtensionAware).extensions.create(
                "printHierarchy",
                KmpHierarchyConfig::class.java
            )

            val config = project.provider { configDsl }

            project.tasks.register("printHierarchy", PrintHmppTask::class.java) { task ->
                task.graphName.set(project.path.replace(':', '-').removePrefix("-"))
                task.fileFormats.set(config.map { it.formats })
                task.withTestHierarchy.set(config.map { it.withTestHierarchy })
                task.targetHierarchy.set(
                    project.provider {
                        kotlinExt.targets
                            .filter { target -> target.platformType != KotlinPlatformType.common }
                            .flatMap { t -> t.compilations }
                            .map { compilation ->
                                TargetInfo(
                                    compilation.target.targetName,
                                    compilation.name,
                                    compilation.allKotlinSourceSets
                                        .filterNot { sourceSet -> isSpecificAndroidSourceSet(compilation, sourceSet) }
                                        .reversed()
                                        .associate { sourceSet -> sourceSet.name to sourceSet.parents() }
                                )
                            }
                    }
                )
                task.outputDir.set(config.flatMap { it.outputDir })
            }
        }
    }
}

private fun KotlinSourceSet.parents(): Set<String> {
    val current = this.name
    return this.dependsOn
        .map { it.name }
        .toMutableSet().apply {
            remove(current)
            if (size > 1) {
                remove(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME)
                remove(KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME)
            }
        }
}

private val onlyCommon = setOf(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME, KotlinSourceSet.COMMON_TEST_SOURCE_SET_NAME)
private fun isSpecificAndroidSourceSet(compilation: KotlinCompilation<*>, sourceSet: KotlinSourceSet): Boolean {
    if (compilation.target !is KotlinAndroidTarget) return false
    if (sourceSet != compilation.defaultSourceSet) return false
    val parents = sourceSet.parents()
    return parents.size == 1 && parents.single() in onlyCommon
}

abstract class KmpHierarchyConfig @Inject constructor(project: Project) {
    internal var formats: Set<Format> = setOf(Format.SVG)

    /**
     * Sets the formats to be used for generating output files.
     *
     * @param formats the formats to be used
     */
    fun formats(vararg formats: Format) {
        this.formats = formats.toSet()
    }

    /**
     * Boolean variable indicating whether to include test hierarchy in the generated output.
     */
    var withTestHierarchy: Boolean = false

    /**
     * The output directory where the generated files will be saved.
     */
    abstract val outputDir: DirectoryProperty

    init {
        outputDir.convention(project.layout.buildDirectory.dir("kmp-hierarchy"))
    }
}

internal data class TargetInfo(
    val targetName: String,
    val compilationName: String,
    val sourceSetsHierarchy: Map<String, Set<String>>
) : Serializable {
    val isTest = compilationName.contains("test", ignoreCase = true)
}

internal abstract class PrintHmppTask : DefaultTask() {
    @get:Input
    abstract val targetHierarchy: ListProperty<TargetInfo>

    @get:Input
    abstract val fileFormats: SetProperty<Format>

    @get:Input
    abstract val withTestHierarchy: Property<Boolean>

    @get:Input
    abstract val graphName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun run() {
        val graphName = graphName.get()
        val formats = fileFormats.get()
        val withTest = withTestHierarchy.get()

        val hierarchy = targetHierarchy.get().filter { info -> withTest || !info.isTest }

        val dir = outputDir.get().asFile
        dir.deleteRecursively()
        dir.mkdirs()

        val g = graph(graphName).with(
            hierarchy.flatMap { targetInfo ->
                targetInfo.sourceSetsHierarchy.flatMap { (s, d) ->
                    d.map { node(it).link(s) }
                }
            }
        )

        formats.forEach { format ->
            val file = File(dir, graphName + ".${format.fileExtension.lowercase()}")
            Graphviz.fromGraph(g).render(format).toFile(file)
            logger.lifecycle("$graphName hierarchy ${format.name}: ${file.toURI()}")
        }
    }
}