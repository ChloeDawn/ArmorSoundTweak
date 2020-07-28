package dev.sapphic.armorsoundtweak;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Blocks;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@Mod("armorsoundtweak")
public final class ArmorSoundTweak {
  private final Minecraft client = Minecraft.getInstance();

  private final ForgeConfigSpec.BooleanValue armor;
  private final ForgeConfigSpec.BooleanValue elytra;
  private final ForgeConfigSpec.BooleanValue skulls;
  private final ForgeConfigSpec.BooleanValue pumpkins;
  private final ForgeConfigSpec.BooleanValue anything;

  private List<Item> oldEquipment = new ArrayList<>(0);

  public ArmorSoundTweak() {
    final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    builder.push("sounds");
    this.armor = builder.define("armor", true);
    this.elytra = builder.define("elytra", true);
    this.skulls = builder.define("skulls", false);
    this.pumpkins = builder.define("pumpkins", false);
    this.anything = builder.define("anything", false);
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
                final Item item = (newItem == Items.AIR) ? oldItem : newItem;
                SoundEvent sound = SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
                if (this.armor.get() && (item instanceof ArmorItem)) {
                  sound = ((ArmorItem) item).getArmorMaterial().getSoundEvent();
                } else if (this.elytra.get() && (item instanceof ElytraItem)) {
                  sound = SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA;
                } else if (!this.pumpkins.get() || (item != Blocks.CARVED_PUMPKIN.asItem())) {
                  if ((!this.skulls.get() || !isSkull(item)) && !this.anything.get()) {
                    continue;
                  }
                }
                this.client.player.playSound(sound, SoundCategory.NEUTRAL, 1.0F, 1.0F);
              }
            }
          }
          this.oldEquipment = equipment;
        }
      }
    });
  }

  private static boolean isSkull(final Item item) {
    return (item instanceof BlockItem) && (((BlockItem) item).getBlock() instanceof AbstractSkullBlock);
  }
}
