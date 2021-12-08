plugins {
    id("co.uzzu.dotenv.gradle") version "1.2.0"
    id("com.gradle.plugin-publish") version "0.18.0"
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "1.6.0"
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

object Artifact {
    const val groupId = "nl.recognize.liquibase.validate"
    const val artifactId = "gradle"
    const val version = "1.0.0"
}

group = Artifact.groupId
version = Artifact.version

gradlePlugin {
    plugins {
        register("liquibase-validate") {
            id = "nl.recognize.liquibase.validate.gradle"
            implementationClass = "nl.recognize.liquibase.validate.gradle.LiquibaseValidatePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/juulhobert/liquibase-validate"
    vcsUrl = "https://github.com/juulhobert/liquibase-validate.git"
    description = "A plugin for checking Liquibase changes"
    tags = listOf("liquibase", "validation", "check")

    (plugins) {
        "liquibase-validate" {
            displayName = "Gradle Liquibase validate plugin"
            version = Artifact.version
        }
    }

    mavenCoordinates {
        groupId = Artifact.groupId
        artifactId = Artifact.artifactId
        version = Artifact.version
    }
}

publishing {
    publishing {
        repositories {
            mavenLocal()
        }

        publications.create("pluginMaven", MavenPublication::class) {
            artifactId = Artifact.artifactId
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