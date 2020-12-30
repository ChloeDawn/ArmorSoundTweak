plugins {
  id("fabric-loom") version "0.5.43"
  id("signing")
}

group = "dev.sapphic"
version = "3.0.0"

dependencies {
  minecraft("com.mojang:minecraft:1.16.4")
  mappings(minecraft.officialMojangMappings())
  modImplementation("net.fabricmc:fabric-loader:0.10.8")
  modImplementation(include("net.fabricmc.fabric-api:fabric-api-base:0.2.0+ab87788d3a")!!)
  modImplementation(include("net.fabricmc.fabric-api:fabric-lifecycle-events-v1:1.2.0+ffb68a873a")!!)
  implementation(include("com.electronwill.night-config:core:3.6.3")!!)
  implementation(include("com.electronwill.night-config:toml:3.6.3")!!)
  implementation("org.checkerframework:checker-qual:3.8.0")
}

tasks {
  compileJava {
    with(options) {
      options.release.set(8)
      isFork = true
      isDeprecation = true
      encoding = "UTF-8"
      compilerArgs.addAll(listOf("-Xlint:all", "-parameters"))
    }
  }

  processResources {
    filesMatching("/fabric.mod.json") {
      expand("version" to project.version)
    }
  }

  jar {
    // FIXME Loom does not respect this
    archiveClassifier.set("fabric")

    from("/LICENSE")

    manifest.attributes(mapOf(
      "Specification-Title" to "MinecraftMod",
      "Specification-Vendor" to project.group,
      "Specification-Version" to "1.0.0",
      "Implementation-Title" to project.name,
      "Implementation-Version" to project.version,
      "Implementation-Vendor" to project.group,
      "Sealed" to "true"
    ))
  }
}

signing {
  sign(configurations.archives.get())
}
