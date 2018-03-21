package net.insomniakitten.ast;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Iterator;
import java.util.List;

@Mod(modid = ArmorSoundTweak.ID,
     name = ArmorSoundTweak.NAME,
     version = ArmorSoundTweak.VERSION,
     acceptedMinecraftVersions = "[1.10,1.13)",
     clientSideOnly = true)
@Mod.EventBusSubscriber(Side.CLIENT)
public final class ArmorSoundTweak {

    public static final String ID = "armorsoundtweak";
    public static final String NAME = "Armor Sound Tweak";
    public static final String VERSION = "%VERSION%";

    private static SoundEvent soundElytraEquip;
    private static List<ItemStack> lastEquipment = Lists.newArrayList();

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        ResourceLocation id = new ResourceLocation("item.armor.equip_elytra");
        SoundEvent sound = SoundEvent.REGISTRY.getObject(id);
        soundElytraEquip = sound != null ? sound : SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = FMLClientHandler.instance().getClient();

        if (event.phase == TickEvent.Phase.START && mc.player != null) {
            List<ItemStack> equipment = Lists.newArrayList();

            for (ItemStack stack : mc.player.getArmorInventoryList()) {
                equipment.add(stack != null ? stack.copy() : null);
            }

            if (mc.currentScreen != null && mc.currentScreen instanceof GuiContainer) {
                Iterator<ItemStack> newStacks = equipment.iterator();
                Iterator<ItemStack> lastStacks = lastEquipment.iterator();

                while (lastStacks.hasNext() && newStacks.hasNext()) {
                    ItemStack newStack = newStacks.next();
                    ItemStack lastStack = lastStacks.next();

                    if (!ItemStack.areItemsEqualIgnoreDurability(newStack, lastStack)) {
                        if (isValidEquipment(newStack)) {
                            playEquipSound(newStack, mc.player);
                        } else if (isValidEquipment(lastStack)) {
                            playEquipSound(lastStack, mc.player);
                        }
                    }
                }
            }

            lastEquipment = equipment;
        }
    }

    private static boolean isValidEquipment(ItemStack stack) {
        if (stack == null) return false;

        Item item = stack.getItem();

        if (item == Items.SKULL) return true;
        if (item instanceof ItemArmor) return true;
        if (item instanceof ItemElytra) return true;
        if (Block.getBlockFromItem(item) == Blocks.PUMPKIN) {
            return true;
        }

        return false;
    }

    private static void playEquipSound(ItemStack stack, EntityPlayer player) {
        Item item = stack.getItem();
        SoundEvent sound = null;

        if (item == Items.SKULL) {
            sound = SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
        } else if (item instanceof ItemArmor) {
            sound = ((ItemArmor) item).getArmorMaterial().getSoundEvent();
        } else if (item instanceof ItemElytra) {
            sound = soundElytraEquip;
        } else if (Block.getBlockFromItem(item) == Blocks.PUMPKIN) {
            sound = SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
        }

        if (sound != null) {
            player.playSound(sound, 1.0F, 1.0F);
        }
    }

}
