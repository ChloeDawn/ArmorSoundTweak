package dev.sapphic.armorsoundtweak.integration.clothconfig;

import dev.sapphic.armorsoundtweak.ArmorSoundTweak;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

public final class ClothConfigIntegration {
  private static final String SOUNDS_CONFIG = ArmorSoundTweak.MOD_ID + "." + "sounds";
  private static final String ARMOR_CONFIG = ArmorSoundTweak.MOD_ID + "." + ArmorSoundTweak.ARMOR;
  private static final String ELYTRA_CONFIG = ArmorSoundTweak.MOD_ID + "." + ArmorSoundTweak.ELYTRA;
  private static final String SKULLS_CONFIG = ArmorSoundTweak.MOD_ID + "." + ArmorSoundTweak.SKULLS;
  private static final String PUMPKINS_CONFIG = ArmorSoundTweak.MOD_ID + "." + ArmorSoundTweak.PUMPKINS;
  private static final String ANYTHING_CONFIG = ArmorSoundTweak.MOD_ID + "." + ArmorSoundTweak.ANYTHING;

  private ClothConfigIntegration() {
  }

  public static Screen buildConfigScreen(final Screen parent) {
    final var screen = ConfigBuilder.create()
        .setTitle(new TranslatableComponent(ArmorSoundTweak.MOD_ID))
        .setSavingRunnable(ArmorSoundTweak::saveConfig)
        .setParentScreen(parent);

    final var entries = screen.entryBuilder();

    screen.getOrCreateCategory(new TranslatableComponent(SOUNDS_CONFIG))
        .addEntry(entries.startBooleanToggle(
                new TranslatableComponent(ARMOR_CONFIG),
                ArmorSoundTweak.armor())
            .setDefaultValue(ArmorSoundTweak.DEFAULT_ARMOR)
            .setSaveConsumer(ArmorSoundTweak::armor)
            .build())

        .addEntry(entries.startBooleanToggle(
                new TranslatableComponent(ELYTRA_CONFIG),
                ArmorSoundTweak.elytra())
            .setDefaultValue(ArmorSoundTweak.DEFAULT_ELYTRA)
            .setSaveConsumer(ArmorSoundTweak::elytra)
            .build())

        .addEntry(entries.startBooleanToggle(
                new TranslatableComponent(SKULLS_CONFIG),
                ArmorSoundTweak.skulls())
            .setDefaultValue(ArmorSoundTweak.DEFAULT_SKULLS)
            .setSaveConsumer(ArmorSoundTweak::skulls)
            .build())

        .addEntry(entries.startBooleanToggle(
                new TranslatableComponent(PUMPKINS_CONFIG),
                ArmorSoundTweak.pumpkins())
            .setDefaultValue(ArmorSoundTweak.DEFAULT_PUMPKINS)
            .setSaveConsumer(ArmorSoundTweak::pumpkins)
            .build())

        .addEntry(entries.startBooleanToggle(
                new TranslatableComponent(ANYTHING_CONFIG),
                ArmorSoundTweak.anything())
            .setDefaultValue(ArmorSoundTweak.DEFAULT_ANYTHING)
            .setSaveConsumer(ArmorSoundTweak::anything)
            .build());

    return screen.build();
  }
}
