pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://files.minecraftforge.net/maven")
    maven("https://jitpack.io")
  }

  resolutionStrategy {
    eachPlugin {
      if ("net.minecraftforge.gradle" == requested.id.id) {
        useModule("net.minecraftforge.gradle:ForgeGradle:${requested.version}")
      }
      if ("net.nemerosa.versioning" == requested.id.id) {
        useModule("com.github.nemerosa:versioning:${requested.version}")
      }
    }
  }
}

rootProject.name = "ArmorSoundTweak"
