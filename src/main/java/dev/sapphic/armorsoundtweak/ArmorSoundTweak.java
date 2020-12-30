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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public final class ArmorSoundTweak implements ClientModInitializer {
  private final Supplier<Boolean> armor;
  private final Supplier<Boolean> elytra;
  private final Supplier<Boolean> skulls;
  private final Supplier<Boolean> pumpkins;
  private final Supplier<Boolean> anything;

  private List<Item> oldEquipment = Collections.emptyList();

  public ArmorSoundTweak() {
    final Path file = FabricLoader.getInstance().getConfigDir().resolve("armorsoundtweak.toml");
    final FileConfig config = FileConfig.builder(file).autoreload().build();
    final ConfigSpec spec = new ConfigSpec();

    spec.define("sounds.armor", true);
    spec.define("sounds.elytra", true);
    spec.define("sounds.skulls", false);
    spec.define("sounds.pumpkins", false);
    spec.define("sounds.anything", false);

    try {
      // No removal as config persists throughout entire runtime
      FileWatcher.defaultInstance().addWatch(file, () -> correct(config, spec));
    } catch (final IOException e) {
      throw new IllegalStateException("Unable to add config correction watcher", e);
    }

    this.armor = () -> config.getOrElse("sounds.armor", true);
    this.elytra = () -> config.getOrElse("sounds.elytra", true);
    this.skulls = () -> config.getOrElse("sounds.skulls", false);
    this.pumpkins = () -> config.getOrElse("sounds.pumpkins", false);
    this.anything = () -> config.getOrElse("sounds.anything", false);

    config.load();
    correct(config, spec);
  }

  private static void correct(final FileConfig config, final ConfigSpec spec) {
    if (!spec.isCorrect(config)) {
      spec.correct(config);
    }
    config.save();
  }

  private static boolean isSkull(final Item item) {
    if (item instanceof BlockItem) {
      final Block block = ((BlockItem) item).getBlock();

      return block instanceof AbstractSkullBlock;
    }
    return false;
  }

  @Override
  public void onInitializeClient() {
    ClientTickEvents.START_CLIENT_TICK.register(client -> {
      if ((client.player != null) && (client.player.level != null) && client.player.level.isClientSide) {
        final List<Item> equipment = new ArrayList<>(4);

        for (final ItemStack stack : client.player.getArmorSlots()) {
          equipment.add(stack.getItem());
        }

        if (client.screen instanceof AbstractContainerScreen<?>) {
          final Iterator<Item> newEquipment = equipment.iterator();
          final Iterator<Item> oldEquipment = this.oldEquipment.iterator();

          while (oldEquipment.hasNext() && newEquipment.hasNext()) {
            final Item newItem = newEquipment.next();
            final Item oldItem = oldEquipment.next();

            if (newItem != oldItem) {
              final @Nullable SoundEvent sound = this.getEquipSound(newItem, oldItem);

              if (sound != null) {
                client.player.playNotifySound(sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
              }
            }
          }
        }
        this.oldEquipment = equipment;
      }
    });
  }

  private @Nullable SoundEvent getEquipSound(final Item newItem, final Item oldItem) {
    final Item item = (newItem == Items.AIR) ? oldItem : newItem;

    if (this.armor.get() && (item instanceof ArmorItem)) {
      return ((ArmorItem) item).getMaterial().getEquipSound();
    }

    if (this.elytra.get() && (item instanceof ElytraItem)) {
      return SoundEvents.ARMOR_EQUIP_ELYTRA;
    }

    if (this.pumpkins.get() && (item == Items.CARVED_PUMPKIN)) {
      return SoundEvents.ARMOR_EQUIP_GENERIC;
    }

    if (this.anything.get() || (this.skulls.get() && isSkull(item))) {
      return SoundEvents.ARMOR_EQUIP_GENERIC;
    }

    return null;
  }
}
