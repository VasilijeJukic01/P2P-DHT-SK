plugins {
    id("java")
    id("application")
    id("io.freefair.lombok") version "5.3.3.3"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.kids"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val avroVersion = "1.11.3"

dependencies {
    // Avro
    implementation("org.apache.avro:avro:$avroVersion")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")

    // Test
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "com.kids.app.servent.MultipleServentStarter")
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("all")
}

tasks.register<Copy>("listJarContents") {
    group = "build"
    description = "Lists the contents of the generated JAR file."

    from(tasks.named<Jar>("jar").get().archiveFile)
    into(layout.buildDirectory.dir("jar-contents"))
}

application {
    mainClass.set("com.kids.app.servent.MultipleServentStarter")
}