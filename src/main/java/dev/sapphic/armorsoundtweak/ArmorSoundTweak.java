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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
@Mod("armorsoundtweak")
public final class ArmorSoundTweak {
  private final Minecraft client = Minecraft.getInstance();

  private final Supplier<Boolean> armor;
  private final Supplier<Boolean> elytra;
  private final Supplier<Boolean> skulls;
  private final Supplier<Boolean> pumpkins;
  private final Supplier<Boolean> anything;

  private List<Item> oldEquipment = Collections.emptyList();

  public ArmorSoundTweak() {
    final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

    builder.push("sounds");
    this.armor = builder.define("armor", true)::get;
    this.elytra = builder.define("elytra", true)::get;
    this.skulls = builder.define("skulls", false)::get;
    this.pumpkins = builder.define("pumpkins", false)::get;
    this.anything = builder.define("anything", false)::get;
    builder.pop();

    ModLoadingContext.get().registerConfig(Type.CLIENT, builder.build(), "armorsoundtweak.toml");

    MinecraftForge.EVENT_BUS.<TickEvent.ClientTickEvent>addListener(event -> {
      if ((event.phase == TickEvent.Phase.START) && (this.client.player != null)) {
        if ((this.client.player.world != null) && this.client.player.world.isRemote) {
          final List<Item> equipment = new ArrayList<>(4);

          for (final ItemStack stack : this.client.player.getArmorInventoryList()) {
            equipment.add(stack.getItem());
          }

          if (this.client.currentScreen instanceof ContainerScreen<?>) {
            final Iterator<Item> newEquipment = equipment.iterator();
            final Iterator<Item> oldEquipment = this.oldEquipment.iterator();

            while (oldEquipment.hasNext() && newEquipment.hasNext()) {
              final Item newItem = newEquipment.next();
              final Item oldItem = oldEquipment.next();

              if (newItem != oldItem) {
                final @Nullable SoundEvent sound = this.getEquipSound(newItem, oldItem);

                if (sound != null) {
                  this.client.player.playSound(sound, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                }
              }
            }
          }
          this.oldEquipment = equipment;
        }
      }
    });
  }

  private static boolean isSkull(final Item item) {
    if (item instanceof BlockItem) {
      final Block block = ((BlockItem) item).getBlock();

      return block instanceof AbstractSkullBlock;
    }
    return false;
  }

  private @Nullable SoundEvent getEquipSound(final Item newItem, final Item oldItem) {
    final Item item = (newItem == Items.AIR) ? oldItem : newItem;

    if (this.armor.get() && (item instanceof ArmorItem)) {
      return ((ArmorItem) item).getArmorMaterial().getSoundEvent();
    }

    if (this.elytra.get() && (item instanceof ElytraItem)) {
      return SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA;
    }

    if (this.pumpkins.get() && (item == Items.CARVED_PUMPKIN)) {
      return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
    }

    if (this.anything.get() || (this.skulls.get() && isSkull(item))) {
      return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
    }

    return null;
  }
}
