rootProject.name = "ArmorSoundTweak"

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://files.minecraftforge.net/maven")
  }
  resolutionStrategy.eachPlugin {
    when (requested.id.id) {
      "net.minecraftforge.gradle.forge" -> {
        useModule("net.minecraftforge.gradle:ForgeGradle:${requested.version}")
      }
    }
  }
}
