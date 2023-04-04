plugins {
    id("co.uzzu.dotenv.gradle") version "1.2.0"
    id("com.gradle.plugin-publish") version "0.18.0"
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "1.6.0"
    id("com.zoltu.git-versioning") version "3.0.3"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(gradleApi())
    testImplementation(gradleTestKit())
    implementation("org.yaml:snakeyaml:1.29")
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    }
    test {
        useJUnitPlatform()
    }
}

val versionInfo = ZoltuGitVersioning.versionInfo
val artifactId = "gradle"

group = "nl.recognizegroup.liquibase.validate"
version = "${versionInfo.major}.${versionInfo.minor}.${versionInfo.commitCount}"

gradlePlugin {
    plugins {
        register("liquibase-validate") {
            id = "nl.recognizegroup.liquibase.validate.gradle"
            implementationClass = "nl.recognizegroup.liquibase.validate.gradle.LiquibaseValidatePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/recognizegroup/liquibase-validate"
    vcsUrl = "https://github.com/recognizegroup/liquibase-validate.git"
    description = "A plugin for checking Liquibase changes"
    tags = listOf("liquibase", "validation", "check")

    (plugins) {
        "liquibase-validate" {
            displayName = "Gradle Liquibase validate plugin"
            version = project.version.toString()
        }
    }

    mavenCoordinates {
        groupId = project.group.toString()
        artifactId = "gradle"
        version = project.version.toString()
    }
}

publishing {
    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/recognizegroup/liquibase-validate")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
            mavenLocal()
        }
        publications {
            create("pluginMaven", MavenPublication::class) {
                artifactId = "gradle"
            }
            create<MavenPublication>("default") {
                from(components["java"])
            }
        }
    }
}

if (env.PUBLISH_PRODUCTION.isPresent) {
    val setPublishingSecrets by tasks.creating {
        doLast {
            System.setProperty("gradle.publish.key", env.GRADLE_PUBLISH_KEY.value)
            System.setProperty("gradle.publish.secret", env.GRADLE_PUBLISH_SECRET.value)
        }
    }
    tasks.getByName("publishPlugins").dependsOn(setPublishingSecrets)
}
