package dev.sapphic.armorsoundtweak;

import me.shedaniel.clothconfig2.forge.api.ConfigBuilder;
import me.shedaniel.clothconfig2.forge.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.function.BiFunction;

final class SoundConfig {
  private static final String AST = ArmorSoundTweak.MOD_ID;
  private static final String SOUNDS = "sounds";
  private static final String ARMOR = "armor";
  private static final String ELYTRA = "elytra";
  private static final String SKULLS = "skulls";
  private static final String PUMPKINS = "pumpkins";
  private static final String ANYTHING = "anything";

  private static final boolean DEFAULT_ARMOR_STATE = true;
  private static final boolean DEFAULT_ELYTRA_STATE = true;
  private static final boolean DEFAULT_SKULLS_STATE = false;
  private static final boolean DEFAULT_PUMPKINS_STATE = false;
  private static final boolean DEFAULT_ANYTHING_STATE = false;

  private final BooleanValue armor;
  private final BooleanValue elytra;
  private final BooleanValue skulls;
  private final BooleanValue pumpkins;
  private final BooleanValue anything;

  private SoundConfig(final ForgeConfigSpec.Builder builder) {
    builder.push(SOUNDS);
    this.armor = builder.define(ARMOR, DEFAULT_ARMOR_STATE);
    this.elytra = builder.define(ELYTRA, DEFAULT_ELYTRA_STATE);
    this.skulls = builder.define(SKULLS, DEFAULT_SKULLS_STATE);
    this.pumpkins = builder.define(PUMPKINS, DEFAULT_PUMPKINS_STATE);
    this.anything = builder.define(ANYTHING, DEFAULT_ANYTHING_STATE);
    builder.pop();
  }

  static SoundConfig create() {
    final ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
    final SoundConfig config = new SoundConfig(configBuilder);
    final ModLoadingContext context = ModLoadingContext.get();

    context.registerConfig(ModConfig.Type.CLIENT, configBuilder.build(), AST + ".toml");

    if (ModList.get().isLoaded("cloth-config")) {
      context.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> config.new ScreenFactory());
    }

    return config;
  }

  boolean hasArmor() {
    return this.hasAnything() || this.armor.get();
  }

  boolean hasElytra() {
    return this.hasAnything() || this.elytra.get();
  }

  boolean hasSkulls() {
    return this.hasAnything() || this.skulls.get();
  }

  boolean hasPumpkins() {
    return this.hasAnything() || this.pumpkins.get();
  }

  boolean hasAnything() {
    return this.anything.get();
  }

  private final class ScreenFactory implements BiFunction<Minecraft, Screen, Screen> {
    @Override
    public Screen apply(final Minecraft minecraft, final Screen parent) {
      final TranslationTextComponent screenTitle = new TranslationTextComponent(AST + ".config");
      final ConfigBuilder configScreenBuilder = ConfigBuilder.create().setTitle(screenTitle);
      final ConfigEntryBuilder entryBuilder = configScreenBuilder.entryBuilder();

      configScreenBuilder.getOrCreateCategory(StringTextComponent.EMPTY)
        .addEntry(entryBuilder.startBooleanToggle(
          new TranslationTextComponent(AST + ".config." + SOUNDS + '.' + ARMOR), SoundConfig.this.armor.get()
        ).setSaveConsumer(SoundConfig.this.armor::set).setDefaultValue(DEFAULT_ARMOR_STATE).build())
        .addEntry(entryBuilder.startBooleanToggle(
          new TranslationTextComponent(AST + ".config." + SOUNDS + '.' + ELYTRA), SoundConfig.this.elytra.get()
        ).setSaveConsumer(SoundConfig.this.elytra::set).setDefaultValue(DEFAULT_ELYTRA_STATE).build())
        .addEntry(entryBuilder.startBooleanToggle(
          new TranslationTextComponent(AST + ".config." + SOUNDS + '.' + SKULLS), SoundConfig.this.skulls.get()
        ).setSaveConsumer(SoundConfig.this.skulls::set).setDefaultValue(DEFAULT_SKULLS_STATE).build())
        .addEntry(entryBuilder.startBooleanToggle(
          new TranslationTextComponent(AST + ".config." + SOUNDS + '.' + PUMPKINS), SoundConfig.this.pumpkins.get()
        ).setSaveConsumer(SoundConfig.this.pumpkins::set).setDefaultValue(DEFAULT_PUMPKINS_STATE).build())
        .addEntry(entryBuilder.startBooleanToggle(
          new TranslationTextComponent(AST + ".config." + SOUNDS + '.' + ANYTHING), SoundConfig.this.anything.get()
        ).setSaveConsumer(SoundConfig.this.anything::set).setDefaultValue(DEFAULT_ANYTHING_STATE).build());

      return configScreenBuilder.setParentScreen(parent).build();
    }
  }
}
