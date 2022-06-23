import java.time.Instant

plugins {
  id("fabric-loom") version "0.12.51"
  id("net.nemerosa.versioning") version "3.0.0"
  id("signing")
}

group = "dev.sapphic"
version = "6.0.0"

java {
  withSourcesJar()
}

loom {
  runs {
    configureEach {
      vmArgs("-Xmx4G", "-XX:+UseZGC")
    }
  }
}

repositories {
  maven("https://maven.shedaniel.me") {
    content {
      includeGroup("me.shedaniel.cloth")
    }
  }

  maven("https://maven.terraformersmc.com/releases") {
    content {
      includeGroup("com.terraformersmc")
    }
  }
}

dependencies {
  minecraft("com.mojang:minecraft:1.19")
  mappings(loom.layered {
    officialMojangMappings {
      nameSyntheticMembers = true
    }
  })

  modImplementation("net.fabricmc:fabric-loader:0.14.8")

  modImplementation(include(fabricApi.module("fabric-api-base", "0.56.2+1.19"))!!)
  modImplementation(include(fabricApi.module("fabric-lifecycle-events-v1", "0.56.2+1.19"))!!)
  modImplementation(include(fabricApi.module("fabric-resource-loader-v0", "0.56.2+1.19"))!!)

  modImplementation(include("me.shedaniel.cloth:cloth-config-fabric:7.0.72") {
    exclude(group = "net.fabricmc.fabric-api")
  })

  modImplementation("com.terraformersmc:modmenu:4.0.0")

  implementation(include("com.electronwill.night-config:core:3.6.5")!!)
  implementation(include("com.electronwill.night-config:toml:3.6.5")!!)

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

  processResources {
    filesMatching("/fabric.mod.json") {
      expand("version" to project.version)
    }
  }

  jar {
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

      "Specification-Title" to "FabricMod",
      "Specification-Version" to "1.0.0",
      "Specification-Vendor" to project.group,

      "Sealed" to "true"
    )
  }

  remapJar {
    archiveClassifier.set("fabric")
  }

  remapSourcesJar {
    archiveClassifier.set("fabric-sources")
  }

  if (hasProperty("signing.mods.keyalias")) {
    val alias = property("signing.mods.keyalias")
    val keystore = property("signing.mods.keystore")
    val password = property("signing.mods.password")

    fun Sign.antSignJar(task: Task) = task.outputs.files.forEach { file ->
      ant.invokeMethod(
        "signjar", mapOf(
          "jar" to file,
          "alias" to alias,
          "storepass" to password,
          "keystore" to keystore,
          "verbose" to true,
          "preservelastmodified" to true
        ))
    }

    val signJar by creating(Sign::class) {
      dependsOn(remapJar)

      doFirst {
        antSignJar(remapJar.get())
      }

      sign(remapJar.get())
    }

    val signSourcesJar by creating(Sign::class) {
      dependsOn(remapSourcesJar)

      doFirst {
        antSignJar(remapSourcesJar.get())
      }

      sign(remapSourcesJar.get())
    }

    assemble {
      dependsOn(signJar, signSourcesJar)
    }
  }
}
