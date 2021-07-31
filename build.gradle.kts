import java.time.Instant
import net.minecraftforge.gradle.common.tasks.SignJar

plugins {
  id("net.minecraftforge.gradle") version "5.1.16"
  id("net.nemerosa.versioning") version "be24b23"
  id("signing")
}

group = "dev.sapphic"
version = "3.1.1"

java {
  withSourcesJar()
}

minecraft {
  mappings("snapshot", "20210309-1.16.5")
  runs {
    create("client") {
      mods.create("armorsoundtweak").source(sourceSets["main"])
      property("forge.logging.console.level", "debug")
    }
    create("server") {
      mods.create("armorsoundtweak").source(sourceSets["main"])
      property("forge.logging.console.level", "debug")
    }
  }
}

repositories {
  maven("https://maven.shedaniel.me") {
    content {
      includeGroup("me.shedaniel.cloth")
    }
  }
  maven("https://maven.theillusivec4.top") {
    content {
      includeGroup("top.theillusivec4.curios")
    }
  }
}

dependencies {
  minecraft("net.minecraftforge:forge:1.16.5-36.2.2")
  implementation("org.checkerframework:checker-qual:3.15.0")
  implementation(fg.deobf("me.shedaniel.cloth:cloth-config-forge:4.11.26"))
  runtimeOnly(fg.deobf("top.theillusivec4.curios:curios-forge:1.16.5-4.0.5.2"))
  compileOnly(fg.deobf("top.theillusivec4.curios:curios-forge:1.16.5-4.0.5.2:api"))
}

tasks {
  compileJava {
    with(options) {
      release.set(8)
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
      "Specification-Version" to "1.1.0",
      "Specification-Vendor" to project.group,

      "Sealed" to true
    )

    finalizedBy("reobfJar")
  }

  val sourcesJar by getting(Jar::class) {
    archiveClassifier.set("forge-${archiveClassifier.get()}")
  }

  if (project.hasProperty("signing.mods.keyalias")) {
    val keyalias = project.property("signing.mods.keyalias") as String
    val keystore = project.property("signing.mods.keystore") as String
    val password = project.property("signing.mods.password") as String

    val signJar by creating(SignJar::class) {
      dependsOn(reobf)

      alias.set(keyalias)
      keyStore.set(keystore)
      keyPass.set(password)
      storePass.set(password)
      inputFile.set(jar.get().archiveFile)
      outputFile.set(inputFile)

      doLast {
        signing.sign(outputFile.get().asFile)
      }
    }

    val signSourcesJar by creating(SignJar::class) {
      dependsOn(sourcesJar)

      alias.set(keyalias)
      keyStore.set(keystore)
      keyPass.set(password)
      storePass.set(password)
      inputFile.set(sourcesJar.archiveFile)
      outputFile.set(inputFile)

      doLast {
        signing.sign(outputFile.get().asFile)
      }
    }

    assemble {
      dependsOn(signJar, signSourcesJar)
    }
  }
}
