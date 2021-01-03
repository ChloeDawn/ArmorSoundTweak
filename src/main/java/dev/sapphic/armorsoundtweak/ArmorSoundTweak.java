package dev.sapphic.armorsoundtweak;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Config(modid = ArmorSoundTweak.MOD_ID, category = "")
@Mod(modid = ArmorSoundTweak.MOD_ID, useMetadata = true,
  // Not included/supported in metadata file schema
  clientSideOnly = true, acceptedMinecraftVersions = "[1.10,1.13)")
public final class ArmorSoundTweak {
  @Config.Ignore
  public static final String MOD_ID = "armorsoundtweak";
  public static final Sounds SOUNDS = new Sounds();

  private final Minecraft client = Minecraft.getMinecraft();
  private List<SubItem> oldEquipment = Collections.emptyList();
  private @MonotonicNonNull SoundEvent elytraEquipSound;

  @EventHandler
  public void preInit(final FMLPreInitializationEvent event) {
    MinecraftForge.EVENT_BUS.register(this);
  }

  @EventHandler
  public void postInit(final FMLPostInitializationEvent event) {
    final ResourceLocation id = new ResourceLocation("item.armor.equip_elytra");
    final @Nullable SoundEvent sound = SoundEvent.REGISTRY.getObject(id);

    this.elytraEquipSound = (sound != null) ? sound : SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
  }

  @SubscribeEvent
  public void configChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
    if (MOD_ID.equals(event.getModID())) {
      ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
    }
  }

  @SubscribeEvent
  public void tick(final TickEvent.ClientTickEvent event) {
    if ((event.phase == TickEvent.Phase.START) && (this.client.player != null)) {
      if ((this.client.player.world != null) && this.client.player.world.isRemote) {
        final List<SubItem> equipment = new ArrayList<>(4);

        for (final ItemStack stack : this.client.player.getArmorInventoryList()) {
          equipment.add((stack != null) ? new SubItem(stack) : SubItem.EMPTY);
        }

        if (this.client.currentScreen instanceof GuiContainer) {
          final Iterator<SubItem> newEquipment = equipment.iterator();
          final Iterator<SubItem> oldEquipment = this.oldEquipment.iterator();

          while (oldEquipment.hasNext() && newEquipment.hasNext()) {
            final SubItem newItem = newEquipment.next();
            final SubItem oldItem = oldEquipment.next();

            if (!newItem.is(oldItem)) {
              final @Nullable SoundEvent sound = this.getEquipSound(newItem, oldItem);

              if (sound != null) {
                this.client.player.playSound(sound, 1.0F, 1.0F);
              }
            }
          }
        }

        this.oldEquipment = equipment;
      }
    }
  }

  private @Nullable SoundEvent getEquipSound(final SubItem newItem, final SubItem oldItem) {
    final Item item = ((newItem.item == Items.AIR) ? oldItem : newItem).item;

    if (SOUNDS.armor && (item instanceof ItemArmor)) {
      return ((ItemArmor) item).getArmorMaterial().getSoundEvent();
    }

    if (SOUNDS.elytra && (item instanceof ItemElytra)) {
      return this.elytraEquipSound;
    }

    if (SOUNDS.pumpkins && (Block.getBlockFromItem(item) == Blocks.PUMPKIN)) {
      return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
    }

    if (SOUNDS.anything || (SOUNDS.skulls && (item instanceof ItemSkull))) {
      return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
    }

    return null;
  }

  public static final class Sounds {
    @Config.Name("armor")
    public boolean armor = true;

    @Config.Name("elytra")
    public boolean elytra = true;

    @Config.Name("skulls")
    public boolean skulls = false;

    @Config.Name("pumpkins")
    public boolean pumpkins = false;

    @Config.Name("anything")
    public boolean anything = false;

    private Sounds() {
    }
  }

  private static final class SubItem {
    private static final SubItem EMPTY = new SubItem(Items.AIR, 0);

    private final Item item;
    private final int metadata;

    private SubItem(final Item item, final int metadata) {
      this.item = item;
      this.metadata = metadata;
    }

    private SubItem(final ItemStack stack) {
      this(stack.getItem(), stack.getMetadata());
    }

    private boolean is(final SubItem that) {
      return (this == that) || ((this.metadata == that.metadata) && (this.item == that.item));
    }
  }
}
