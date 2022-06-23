package dev.sapphic.armorsoundtweak.integration.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.sapphic.armorsoundtweak.ArmorSoundTweak;
import dev.sapphic.armorsoundtweak.integration.clothconfig.ClothConfigIntegration;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Function;
import java.util.function.Supplier;

public final class ModMenuIntegration implements ModMenuApi {
  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return parent -> buildOrOpen(parent, () -> ClothConfigIntegration::buildConfigScreen);
  }

  private static Screen buildOrOpen(final Screen parent, final Supplier<Function<Screen, Screen>> builder) {
    return FabricLoader.getInstance().isModLoaded("cloth-config")
        ? builder.get().apply(parent)
        : new ConfirmLinkScreen(ok -> {
          if (ok) {
            Util.getPlatform().openUri(ArmorSoundTweak.CONFIG_FILE.toUri());
          }

          Minecraft.getInstance().setScreen(parent);
        }, ArmorSoundTweak.CONFIG_FILE.toString(), true);
  }
}
