import org.gradle.util.GradleVersion
import java.time.Instant
import net.minecraftforge.gradle.common.task.SignJar

plugins {
  id("net.minecraftforge.gradle") version "3.0.190"
  id("net.nemerosa.versioning") version "2.6.1"
  id("signing")
}

group = "dev.sapphic"
version = "3.1.0"

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

repositories {
  jcenter {
    content {
      includeGroup("me.shedaniel.cloth")
    }
  }
}

dependencies {
  minecraft("net.minecraftforge:forge:1.16.4-35.1.37")
  implementation("org.checkerframework:checker-qual:3.8.0")
  implementation(fg.deobf("me.shedaniel.cloth:cloth-config-forge:4.1.3"))
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

    from("/LICENSE")

    manifest.attributes(
      "Build-Timestamp" to Instant.now(),
      "Build-Revision" to versioning.info.commit,
      "Build-Jvm" to "${
        System.getProperty("java.version")
      } (${
        System.getProperty("java.vendor")
      } ${
        System.getProperty("java.vm.version")
      })",
      "Built-By" to GradleVersion.current(),

      "Implementation-Title" to project.name,
      "Implementation-Version" to project.version,
      "Implementation-Vendor" to project.group,

      "Specification-Title" to "ForgeMod",
      "Specification-Version" to "1.0.0",
      "Specification-Vendor" to project.group
    )

    finalizedBy("reobfJar")
  }

  create<SignJar>("signJar") {
    dependsOn("reobfJar")

    setAlias("${project.property("signing.mods.keyalias")}")
    setKeyStore("${project.property("signing.mods.keystore")}")
    setKeyPass("${project.property("signing.mods.password")}")
    setStorePass("${project.property("signing.mods.password")}")
    setInputFile(jar.get().archiveFile.get())
    setOutputFile(inputFile)
  }

  assemble {
    dependsOn("signJar")
  }
}

signing {
  sign(configurations.archives.get())
}
