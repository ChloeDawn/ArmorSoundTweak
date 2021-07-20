package dev.sapphic.armorsoundtweak;

import com.google.common.base.Suppliers;
import me.shedaniel.clothconfig2.forge.api.ConfigBuilder;
import me.shedaniel.clothconfig2.forge.api.ConfigCategory;
import me.shedaniel.clothconfig2.forge.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.forge.gui.entries.BooleanListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class EquipmentConfig {
  private final Slots slots;
  private final Sounds sounds;

  private EquipmentConfig(final ForgeConfigSpec.Builder builder) {
    this.slots = new Slots(builder);
    builder.comment("The types of equip sounds to be played for equipment").push("sounds");
    this.sounds = new Sounds(builder);
    builder.pop();
  }

  static Supplier<EquipmentConfig> lazy() {
    return Suppliers.memoize(() -> {
      final ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
      final EquipmentConfig config = new EquipmentConfig(configBuilder);
      final ModLoadingContext context = ModLoadingContext.get();

      context.registerConfig(ModConfig.Type.CLIENT, configBuilder.build(), ArmorSoundTweak.MOD_ID + ".toml");

      if (ModList.get().isLoaded("cloth-config")) {
        context.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> new ConfigGuiFactory(config));
      }

      return config;
    });
  }

  public boolean allowsArmor() {
    return this.slots.armor.get();
  }

  public boolean allowsCurios() {
    return this.slots.curios.get();
  }

  public Sounds sounds() {
    return this.sounds;
  }

  public static final class Slots {
    private final BooleanValue armor;
    private final BooleanValue curios;

    Slots(final ForgeConfigSpec.Builder builder) {
      this.armor = builder.define("armor", true);
      this.curios = builder.define("curios", true);
    }
  }

  public static final class Sounds {
    private final BooleanValue armor;
    private final BooleanValue elytra;
    private final BooleanValue skulls;
    private final BooleanValue pumpkins;
    private final BooleanValue anything;

    Sounds(final ForgeConfigSpec.Builder builder) {
      this.armor = builder.define("armor", true);
      this.elytra = builder.define("elytra", true);
      this.skulls = builder.define("skulls", false);
      this.pumpkins = builder.define("pumpkins", false);
      this.anything = builder.define("anything", false);
    }

    public boolean hasArmor() {
      return this.armor.get();
    }

    public boolean hasElytra() {
      return this.elytra.get();
    }

    public boolean hasSkulls() {
      return this.skulls.get();
    }

    public boolean hasPumpkins() {
      return this.pumpkins.get();
    }

    public boolean hasAnything() {
      return this.anything.get();
    }
  }

  private static final class ConfigGuiFactory implements BiFunction<Minecraft, Screen, Screen> {
    private final EquipmentConfig config;

    private ConfigGuiFactory(final EquipmentConfig config) {
      this.config = config;
    }

    private static ConfigCategory category(final ConfigBuilder config, final String name) {
      return config.getOrCreateCategory(new TranslationTextComponent(ArmorSoundTweak.MOD_ID + ".config." + name));
    }

    private static BooleanListEntry bool(final ConfigEntryBuilder builder, final String name, final BooleanValue value, final boolean defaultValue) {
      return builder.startBooleanToggle(
        new TranslationTextComponent(ArmorSoundTweak.MOD_ID + ".config." + name), value.get()
      ).setSaveConsumer(value::set).setDefaultValue(defaultValue).build();
    }

    @Override
    public Screen apply(final Minecraft minecraft, final Screen screen) {
      final ConfigBuilder config = ConfigBuilder.create()
        .setTitle(new TranslationTextComponent(ArmorSoundTweak.MOD_ID + ".config"));
      final ConfigEntryBuilder entries = config.entryBuilder();

      final Slots slots = this.config.slots;

      category(config, "slots")
        .addEntry(bool(entries, "slots.curios", slots.curios, true))
        .addEntry(bool(entries, "slots.armor", slots.armor, true));

      final Sounds sounds = this.config.sounds;

      category(config, "sounds")
        .addEntry(bool(entries, "sounds.armor", sounds.armor, true))
        .addEntry(bool(entries, "sounds.elytra", sounds.elytra, true))
        .addEntry(bool(entries, "sounds.skulls", sounds.skulls, false))
        .addEntry(bool(entries, "sounds.pumpkins", sounds.pumpkins, false))
        .addEntry(bool(entries, "sounds.anything", sounds.anything, false));

      return config.build();
    }
  }
}
