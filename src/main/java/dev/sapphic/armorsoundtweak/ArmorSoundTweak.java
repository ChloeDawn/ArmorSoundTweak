package dev.sapphic.armorsoundtweak;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.file.FileWatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractSkullBlock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class ArmorSoundTweak implements ClientModInitializer {
  public static final String MOD_ID = "armorsoundtweak";

  public static final String ARMOR = "sounds.armor";
  public static final String ELYTRA = "sounds.elytra";
  public static final String SKULLS = "sounds.skulls";
  public static final String PUMPKINS = "sounds.pumpkins";
  public static final String ANYTHING = "sounds.anything";

  public static final boolean DEFAULT_ARMOR = true;
  public static final boolean DEFAULT_ELYTRA = true;
  public static final boolean DEFAULT_SKULLS = false;
  public static final boolean DEFAULT_PUMPKINS = false;
  public static final boolean DEFAULT_ANYTHING = false;

  public static final Path CONFIG_FILE =
      FabricLoader.getInstance().getGameDir()
          .relativize(FabricLoader.getInstance().getConfigDir())
          .resolve(MOD_ID + ".toml");

  private static final Logger LOGGER = LogManager.getLogger();
  private static final FileConfig CONFIG = FileConfig.builder(CONFIG_FILE).build();
  private static final ConfigSpec CONFIG_SPEC = new ConfigSpec();

  static {
    CONFIG_SPEC.define(ARMOR, DEFAULT_ARMOR);
    CONFIG_SPEC.define(ELYTRA, DEFAULT_ELYTRA);
    CONFIG_SPEC.define(SKULLS, DEFAULT_SKULLS);
    CONFIG_SPEC.define(PUMPKINS, DEFAULT_PUMPKINS);
    CONFIG_SPEC.define(ANYTHING, DEFAULT_ANYTHING);

    try {
      FileWatcher.defaultInstance().addWatch(CONFIG_FILE, ArmorSoundTweak::reloadConfig);
    } catch (final IOException e) {
      throw new IllegalStateException("Unable to add file watcher", e);
    }

    loadConfig();
  }

  private List<ItemStack> oldEquipment = Collections.emptyList();

  public static boolean armor() {
    return CONFIG.get(ARMOR);
  }

  public static void armor(final boolean armor) {
    CONFIG.set(ARMOR, armor);
  }

  public static boolean elytra() {
    return CONFIG.get(ELYTRA);
  }

  public static void elytra(final boolean elytra) {
    CONFIG.set(ELYTRA, elytra);
  }

  public static boolean skulls() {
    return CONFIG.get(SKULLS);
  }

  public static void skulls(final boolean skulls) {
    CONFIG.set(SKULLS, skulls);
  }

  public static boolean pumpkins() {
    return CONFIG.get(PUMPKINS);
  }

  public static void pumpkins(final boolean pumpkins) {
    CONFIG.set(PUMPKINS, pumpkins);
  }

  public static boolean anything() {
    return CONFIG.get(ANYTHING);
  }

  public static void anything(final boolean anything) {
    CONFIG.set(ANYTHING, anything);
  }

  public static void saveConfig() {
    LOGGER.debug("Saving config to file {}", CONFIG_FILE);
    CONFIG.save();
  }

  private static void loadConfig0() {
    CONFIG.load();

    if (CONFIG_SPEC.correct(CONFIG, ArmorSoundTweak::logCorrections) > 0) {
      saveConfig();
    }
  }

  private static void loadConfig() {
    LOGGER.debug("Loading config from file {}", CONFIG_FILE);
    loadConfig0();
  }

  private static void reloadConfig() {
    LOGGER.debug("Reloading config from file {}", CONFIG_FILE);
    loadConfig0();
  }

  private static @Nullable SoundEvent getEquipSound(final ItemStack newItem, final ItemStack oldItem) {
    final var item = (newItem.isEmpty() ? oldItem : newItem).getItem();

    if ((armor() && (item instanceof ArmorItem)) || (elytra() && (item instanceof ElytraItem))) {
      return item.getEquipSound();
    }

    if ((pumpkins() && (item == Items.CARVED_PUMPKIN)) || anything() || (skulls() && isSkull(item))) {
      return SoundEvents.ARMOR_EQUIP_GENERIC;
    }

    return null;
  }

  private static boolean isSkull(final Item item) {
    return (item instanceof BlockItem) && (((BlockItem) item).getBlock() instanceof AbstractSkullBlock);
  }

  private static void logCorrections(
      final ConfigSpec.CorrectionAction action,
      final List<String> path,
      final @Nullable Object incorrectValue,
      final @Nullable Object correctedValue) {
    switch (action) {
      case REPLACE -> LOGGER.debug("Defaulting invalid value '{} = {}' to '{}'",
          String.join(".", path), incorrectValue, correctedValue);
      case REMOVE -> LOGGER.debug("Removing unrecognized option '{} = {}'",
          String.join(".", path), incorrectValue);
    }
  }

  @Override
  public void onInitializeClient() {
    ClientTickEvents.START_CLIENT_TICK.register(client -> {
      final @Nullable Player player = client.player;

      if ((player != null) && (player.level != null) && player.level.isClientSide) {
        final List<ItemStack> equipment = new ArrayList<>(4);

        for (final var stack : player.getArmorSlots()) {
          equipment.add(stack.copy());
        }

        if (client.screen instanceof AbstractContainerScreen<?>) {
          final var newEquipment = equipment.iterator();
          final var oldEquipment = this.oldEquipment.iterator();

          while (oldEquipment.hasNext() && newEquipment.hasNext()) {
            final var newItem = newEquipment.next();
            final var oldItem = oldEquipment.next();

            if (!newItem.is(oldItem.getItem())) {
              final @Nullable SoundEvent sound = getEquipSound(newItem, oldItem);

              if (sound != null) {
                player.playSound(sound, 1.0F, 1.0F);
              }
            }
          }
        }

        this.oldEquipment = equipment;
      }
    });
  }
}
