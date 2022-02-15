import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    id("edu.sc.seis.launch4j") version "2.5.0"
}

group = "me.qaz"
val ver = "1.1.0"
version = ver

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.github.weisj:darklaf-core:2.7.3")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

// Unix build
tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

// Windows build
launch4j {
    mainClassName = "MainKt"
    internalName = project.name
    productName = project.name
    textVersion = ver
    icon = "$projectDir/src/main/resources/icons.ico"
    fileDescription = "A tool for removing watermarks in SVG files created by the StarUML software"
    copyright = "MIT"
}

// All platform build
tasks.register<GradleBuild>("export") {
    tasks = listOf("jar", "createExe")
}