import net.minecraftforge.gradle.tasks.SignJar

plugins {
  id("net.minecraftforge.gradle.forge") version "2.3-SNAPSHOT"
  id("signing")
}

version = "2.1.0"
group = "dev.sapphic"

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = sourceCompatibility
}

minecraft {
  version = "1.12.2-14.23.5.2847"
  mappings = "stable_39"
  runDir = "run"
}

signing {
  sign(configurations.archives)
}

repositories {
  maven("https://cursemaven.com")
  maven("https://dvs1.progwml6.com/files/maven")
}

dependencies {
  implementation("curse.maven:hwyla-253449:2568751")
  implementation("mezz.jei:jei_1.12.2:4.16.1.302")
  implementation("org.checkerframework:checker-qual:3.8.0")
}

tasks {
  named<JavaCompile>("compileJava") {
    with(options) {
      isFork = true
      isDeprecation = true
      encoding = "UTF-8"
      compilerArgs.addAll(listOf("-Xlint:all", "-parameters"))
    }
  }

  named<ProcessResources>("processResources") {
    filesMatching(setOf("/mcmod.info", "/version.properties")) {
      expand("version" to project.version)
    }
  }

  named<Jar>("jar") {
    from("/LICENSE")
    manifest.attributes(mapOf(
      "Specification-Title" to "MinecraftMod",
      "Specification-Vendor" to project.group,
      "Specification-Version" to "1.0.0",
      "Implementation-Title" to project.name,
      "Implementation-Version" to project.version,
      "Implementation-Vendor" to project.group
    ))
  }

  named<Sign>("signArchives") {
    onlyIf { project.hasProperty("signing.keyId") }
  }

  create<SignJar>("signJar") {
    onlyIf { project.hasProperty("signing.mods.keyalias") }

    dependsOn("reobfJar")

    setAlias("${project.property("signing.mods.keyalias")}")
    setKeyStore("${project.property("signing.mods.keystore")}")
    setKeyPass("${project.property("signing.mods.password")}")
    setStorePass("${project.property("signing.mods.password")}")
    setInputFile(named<Jar>("jar").get().archivePath)
    setOutputFile(inputFile)
  }

  named</*Assemble*/Task>("assemble") {
    dependsOn("signJar")
  }
}
