import net.minecraftforge.gradle.common.task.SignJar
import org.gradle.jvm.tasks.Jar

plugins {
  id("net.minecraftforge.gradle") version "3.0.189"
  id("signing")
}

group = "dev.sapphic"
version = "3.0.0"

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = sourceCompatibility
}

minecraft {
  mappings("snapshot", "20201028-1.16.3")
  runs {
    create("client") {
      workingDirectory = file("run").canonicalPath
      mods.create("armorsoundtweak").source(sourceSets["main"])
    }
  }
}

dependencies {
  minecraft("net.minecraftforge:forge:1.16.4-35.1.28")
  implementation("org.checkerframework:checker-qual:3.8.0")
}

tasks {
  compileJava {
    with(options) {
      isFork = true
      isDeprecation = true
      encoding = "UTF-8"
      compilerArgs.addAll(listOf("-Xlint:all", "-parameters"))
    }
  }

  jar {
    archiveClassifier.set("forge")

    manifest.attributes(mapOf(
      "Specification-Title" to "MinecraftMod",
      "Specification-Vendor" to project.group,
      "Specification-Version" to "1.0.0",
      "Implementation-Title" to project.name,
      "Implementation-Version" to project.version,
      "Implementation-Vendor" to project.group
    ))

    finalizedBy("reobfJar")
  }

  create<SignJar>("signJar") {
    dependsOn("reobfJar")

    setAlias("${project.property("signing.mods.keyalias")}")
    setKeyStore("${project.property("signing.mods.keystore")}")
    setKeyPass("${project.property("signing.mods.password")}")
    setStorePass("${project.property("signing.mods.password")}")
    setInputFile(named<Jar>("jar").get().archiveFile.get())
    setOutputFile(inputFile)
  }

  assemble {
    dependsOn("signJar")
  }
}

signing {
  sign(configurations.archives.get())
}
