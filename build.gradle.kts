import java.time.Instant
import net.minecraftforge.gradle.common.tasks.SignJar

plugins {
  id("net.minecraftforge.gradle") version "5.1.48"
  id("net.nemerosa.versioning") version "3.0.0"
  id("org.gradle.signing")
}

group = "dev.sapphic"
version = "6.0.0"

java {
  withSourcesJar()
}

minecraft {
  mappings("official", "1.19")

  runs {
    listOf("client", "server").forEach {
      create(it) {
        mods.create("armorsoundtweak").source(sourceSets["main"])
        property("forge.logging.console.level", "debug")
        property("forge.logging.markers", "SCAN")
      }
    }
  }
}

repositories {
  mavenCentral()

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

  maven("https://cursemaven.com") {
    content {
      includeGroup("curse.maven")
    }
  }
}

dependencies {
  minecraft("net.minecraftforge:forge:1.19-41.0.45")

  implementation(fg.deobf("me.shedaniel.cloth:cloth-config-forge:7.0.72"))

  runtimeOnly(fg.deobf("top.theillusivec4.curios:curios-forge:1.19-5.1.0.2"))
  compileOnly(fg.deobf("top.theillusivec4.curios:curios-forge:1.19-5.1.0.2:api"))

  // Curios' debug items were removed in 1.17 so we use this for testing
  runtimeOnly(fg.deobf("curse.maven:technobauble-492052:3836078")) // 0.5.0.1
  runtimeOnly(fg.deobf("curse.maven:bdlib-70496:3836059")) // 1.20.0.3
  runtimeOnly(fg.deobf("curse.maven:scalable-cats-force-320926:3759354")) // 2.13.8-build-4

  implementation("org.checkerframework:checker-qual:3.22.1")
}

tasks {
  compileJava {
    with(options) {
      isDeprecation = true
      encoding = "UTF-8"
      isFork = true
      compilerArgs.addAll(
        listOf(
          "-Xlint:all", "-Xlint:-processing",
          // Enable parameter name class metadata 
          // https://openjdk.java.net/jeps/118
          "-parameters"
        )
      )
      release.set(17)
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
    archiveClassifier.set("forge-sources")
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
