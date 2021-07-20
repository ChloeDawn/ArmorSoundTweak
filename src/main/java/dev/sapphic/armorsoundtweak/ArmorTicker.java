package dev.sapphic.armorsoundtweak;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.entity.player.PlayerEntity;
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
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

final class ArmorTicker extends EquipmentTicker {
  private final EquipmentConfig.Sounds sounds = ArmorSoundTweak.config().sounds();

  static void register() {
    MinecraftForge.EVENT_BUS.addListener(new ArmorTicker());
  }

  private static boolean isSkull(final Item item) {
    return item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof AbstractSkullBlock;
  }

  @Override
  protected List<Item> getEquipment(final PlayerEntity player) {
    final List<Item> equipment = new ArrayList<>(4);

    for (final ItemStack stack : player.getArmorInventoryList()) {
      equipment.add(stack.getItem());
    }

    return equipment;
  }

  @Override
  protected void playEquipSound(final PlayerEntity player, final Item item) {
    if (!ArmorSoundTweak.config().allowsArmor()) {
      return;
    }

    final @Nullable SoundEvent sound = this.getEquipSound(item);

    if (sound != null) {
      player.playSound(sound, SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }
  }

  private @Nullable SoundEvent getEquipSound(final Item item) {
    if (this.sounds.hasArmor() && (item instanceof ArmorItem)) {
      return ((ArmorItem) item).getArmorMaterial().getSoundEvent();
    }

    if (this.sounds.hasElytra() && (item instanceof ElytraItem)) {
      return SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA;
    }

    if (this.sounds.hasPumpkins() && (item == Items.CARVED_PUMPKIN)) {
      return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
    }

    if (this.sounds.hasSkulls() && isSkull(item)) {
      return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
    }

    if (this.sounds.hasAnything()) {
      return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
    }

    return null;
  }
}
