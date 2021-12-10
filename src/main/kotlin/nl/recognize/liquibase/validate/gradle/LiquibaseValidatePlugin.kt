package nl.recognize.liquibase.validate.gradle

import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

class LiquibaseValidatePlugin: Plugin<Project> {
    override fun apply(target: Project) {
        target.getTasksByName("diffChangeLog", true).forEach {
            it.apply {
                doFirst {
                    val changeLogFile = File("${target.rootDir}/src/main/resources/db/changelog/changes.yaml")
                    if (changeLogFile.delete()) {
                        println("Deleted existing changes.yaml")
                    }
                }
                doLast {
                    try {
                        val yaml = Yaml(
                            DumperOptions().apply {
                                defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                            }
                        )

                        val changeLogFile = File("${target.rootDir}/src/main/resources/db/changelog/changes.yaml")
                        val changeLogData = changeLogFile.inputStream().use {
                            yaml.load<Map<String, MutableList<Map<String, Map<String, List<Map<String, Any>>>>>>>(it)
                        }

                        var changeLogUpdated = false
                        val removeChangeSets = mutableListOf<Any>()
                        changeLogData["databaseChangeLog"]?.let { databaseChangeLog ->
                            databaseChangeLog.forEach { changeSet ->
                                changeSet["changeSet"]?.get("changes")?.forEach { change ->
                                    // remove empty alterSequence blocks
                                    if (change.containsKey("alterSequence")) {
                                        @Suppress("UNCHECKED_CAST")
                                        val sequenceChange = change["alterSequence"] as Map<String, Any>
                                        if (sequenceChange.keys.size == 1 && sequenceChange.keys.first() == "sequenceName") {
                                            removeChangeSets.add(changeSet)
                                            changeLogUpdated = true
                                        }
                                    }
                                }
                            }

                            databaseChangeLog.removeAll(removeChangeSets)
                        }

                        if (changeLogUpdated) {
                            changeLogFile.writer().use { writer ->
                                yaml.dump(changeLogData, writer)
                            }
                        }
                    } catch (e: Throwable) {
                        logger.error("cleanupChangeLog failed", e)
                    }
                }
            }
            target.tasks.register("validateDiffChangeLogEmpty") { task ->
                task.dependsOn("diffChangeLog")
                task.doLast {
                    val yaml = Yaml(
                        DumperOptions().apply {
                            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
                        }
                    )

                    val changeLogFile = File("${target.rootDir}/src/main/resources/db/changelog/changes.yaml")
                    val changeLogData = changeLogFile.inputStream().use {
                        yaml.load<Map<String, MutableList<Map<String, Map<String, List<Map<String, Any>>>>>>>(it)
                    }

                    if (!changeLogData["databaseChangeLog"].isNullOrEmpty()) {
                        println("Changelog was invalid or not empty, changelog content:")
                        changeLogFile.forEachLine {
                            println(">  $it")
                        }
                        throw TaskExecutionException(it, Exception("Changelog was invalid or not empty"))
                    }
                }
            }
            target.tasks.named("check") { task ->
                task.dependsOn.add("validateDiffChangeLogEmpty")
            }
        }
    }
}
