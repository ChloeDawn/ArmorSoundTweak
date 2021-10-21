package dev.sapphic.armorsoundtweak;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraftforge.common.MinecraftForge;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;

final class ArmorTicker extends EquipmentTicker {
  private final EquipmentConfig.Sounds sounds = ArmorSoundTweak.config().sounds();

  static void register() {
    MinecraftForge.EVENT_BUS.addListener(new ArmorTicker());
  }

  private static boolean isSkull(final Item item) {
    return (item instanceof BlockItem) && (((BlockItem) item).getBlock() instanceof AbstractSkullBlock);
  }

  @Override
  protected Iterable<ItemStack> getEquipment(final Player player) {
    final var equipment = new ArrayList<ItemStack>(4);

    for (final var stack : player.getArmorSlots()) {
      equipment.add(stack.copy());
    }

    return equipment;
  }

  @Override
  protected void playEquipSound(final Player player, final Item item) {
    if (!ArmorSoundTweak.config().allowsArmor()) {
      return;
    }

    final @Nullable SoundEvent sound = this.getEquipSound(item);

    if (sound != null) {
      player.playNotifySound(sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }
  }

  private @Nullable SoundEvent getEquipSound(final Item item) {
    if (this.sounds.hasArmor() && (item instanceof ArmorItem)) {
      return ((ArmorItem) item).getMaterial().getEquipSound();
    }

    if (this.sounds.hasElytra() && (item instanceof ElytraItem)) {
      return SoundEvents.ARMOR_EQUIP_ELYTRA;
    }

    if (this.sounds.hasPumpkins() && (item == Items.CARVED_PUMPKIN)) {
      return SoundEvents.ARMOR_EQUIP_GENERIC;
    }

    if (this.sounds.hasSkulls() && isSkull(item)) {
      return SoundEvents.ARMOR_EQUIP_GENERIC;
    }

    if (this.sounds.hasAnything()) {
      return SoundEvents.ARMOR_EQUIP_GENERIC;
    }

    return null;
  }
}
