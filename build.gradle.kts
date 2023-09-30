import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.10"
    id("edu.sc.seis.launch4j") version "2.5.0"
}

group = "me.qaz"
version = "1.3.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.apache.pdfbox:pdfbox:2.0.29")
    implementation("com.github.weisj:darklaf-core:2.7.3")
    implementation("org.kie.modules:org-apache-batik:6.1.0.Beta1")
}

tasks.test {
    useJUnit()
}
tasks.withType<JavaCompile>() {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
}

tasks.withType<KotlinCompile>() {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
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
    textVersion = version
    icon = "$projectDir/src/main/resources/icons.ico"
    fileDescription = "A tool for removing watermarks in SVG files created by the StarUML software"
    copyright = "GPL-3.0"
}

// All platform build
tasks.register<GradleBuild>("export") {
    tasks = listOf("jar", "createExe")
}
