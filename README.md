# Liquibase validate Gradle

![master](https://github.com/recognizegroup/liquibase-validate/workflows/main/badge.svg)

**Sanitizes and validates Liquibase changes yaml.**

## How to use

### Setup

Apply this plugin to the project.
This plugin is not registered to Maven Central.
[Read the Gradle Plugin Portal to setup plugin.](https://plugins.gradle.org/plugin/nl.recognize.liquibase.validate.gradle)

After installation the task `validateDiffChangeLogEmpty` is available and it automatically adds a doLast to the `diffChangeLog` which sanitizes the changes.yaml

## License

[LGPL-3.0 License](/LICENSE.txt)
