package dev.sapphic.armorsoundtweak;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

final class EquipmentTicker implements Consumer<TickEvent.ClientTickEvent> {
  private final SoundConfig soundConfig = SoundConfig.create();
  private List<Item> oldEquipment = Collections.emptyList();

  static void register() {
    MinecraftForge.EVENT_BUS.addListener(new EquipmentTicker());
  }

  private static boolean isSkull(final Item item) {
    if (item instanceof BlockItem) {
      final Block block = ((BlockItem) item).getBlock();

      return block instanceof AbstractSkullBlock;
    }

    return false;
  }

  @Override
  public void accept(final TickEvent.ClientTickEvent event) {
    final Minecraft client = Minecraft.getInstance();

    if ((event.phase == TickEvent.Phase.START) && (client.player != null)) {
      if ((client.player.world != null) && client.player.world.isRemote) {
        final List<Item> equipment = new ArrayList<>(4);

        for (final ItemStack stack : client.player.getArmorInventoryList()) {
          equipment.add(stack.getItem());
        }

        if (client.currentScreen instanceof ContainerScreen<?>) {
          final Iterator<Item> newEquipment = equipment.iterator();
          final Iterator<Item> oldEquipment = this.oldEquipment.iterator();

          while (oldEquipment.hasNext() && newEquipment.hasNext()) {
            final Item newItem = newEquipment.next();
            final Item oldItem = oldEquipment.next();

            if (newItem != oldItem) {
              final @Nullable SoundEvent sound = this.getEquipSound(newItem, oldItem);

              if (sound != null) {
                client.player.playSound(sound, SoundCategory.NEUTRAL, 1.0F, 1.0F);
              }
            }
          }
        }

        this.oldEquipment = equipment;
      }
    }
  }

  private @Nullable SoundEvent getEquipSound(final Item newItem, final Item oldItem) {
    final Item item = (newItem == Items.AIR) ? oldItem : newItem;

    if (this.soundConfig.hasArmor() && (item instanceof ArmorItem)) {
      return ((ArmorItem) item).getArmorMaterial().getSoundEvent();
    }

    if (this.soundConfig.hasElytra() && (item instanceof ElytraItem)) {
      return SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA;
    }

    if (this.soundConfig.hasPumpkins() && (item == Items.CARVED_PUMPKIN)) {
      return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
    }

    if (this.soundConfig.hasSkulls() && isSkull(item)) {
      return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
    }

    if (this.soundConfig.hasAnything()) {
      return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
    }

    return null;
  }
}
