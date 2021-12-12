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
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public final class ArmorSoundTweak implements ClientModInitializer {
  private final Supplier<Boolean> armor;
  private final Supplier<Boolean> elytra;
  private final Supplier<Boolean> skulls;
  private final Supplier<Boolean> pumpkins;
  private final Supplier<Boolean> anything;

  private List<ItemStack> oldEquipment = Collections.emptyList();

  public ArmorSoundTweak() {
    final var file = FabricLoader.getInstance().getConfigDir().resolve("armorsoundtweak.toml");
    final var config = FileConfig.builder(file).autoreload().build();
    final var spec = new ConfigSpec();

    final var armor = "sounds.armor";
    final var elytra = "sounds.elytra";
    final var skulls = "sounds.skulls";
    final var pumpkins = "sounds.pumpkins";
    final var anything = "sounds.anything";

    spec.define(armor, true);
    spec.define(elytra, true);
    spec.define(skulls, false);
    spec.define(pumpkins, false);
    spec.define(anything, false);

    try {
      // No removal as config persists throughout entire runtime
      FileWatcher.defaultInstance().addWatch(file, () -> correct(config, spec));
    } catch (final IOException e) {
      throw new IllegalStateException("Unable to add config correction watcher", e);
    }

    this.armor = () -> config.getOrElse(armor, true);
    this.elytra = () -> config.getOrElse(elytra, true);
    this.skulls = () -> config.getOrElse(skulls, false);
    this.pumpkins = () -> config.getOrElse(pumpkins, false);
    this.anything = () -> config.getOrElse(anything, false);

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
    return (item instanceof BlockItem) && (((BlockItem) item).getBlock() instanceof AbstractSkullBlock);
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

            if (!ItemStack.matches(newItem, oldItem)) {
              final @Nullable SoundEvent sound = this.getEquipSound(newItem, oldItem);

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

  private @Nullable SoundEvent getEquipSound(final ItemStack newItem, final ItemStack oldItem) {
    final var item = (newItem.isEmpty() ? oldItem : newItem).getItem();

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
