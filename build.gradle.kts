plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories { mavenCentral() }

val javafxVersion = "22"

javafx {
    version = javafxVersion
    modules = listOf("javafx.controls")
}

application {
    mainClass.set("com.alwaleed.afx.demo.DemoApp")
}