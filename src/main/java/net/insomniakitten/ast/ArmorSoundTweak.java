package net.insomniakitten.ast;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Mod(modid = ArmorSoundTweak.ID, name = ArmorSoundTweak.NAME, version = ArmorSoundTweak.VERSION, clientSideOnly = true)
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
        soundElytraEquip = Optional.ofNullable(SoundEvent.REGISTRY.getObject(id))
                .orElse(SoundEvents.ITEM_ARMOR_EQUIP_GENERIC);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = FMLClientHandler.instance().getClient();

        if (event.phase == TickEvent.Phase.START && mc.player != null) {
            List<ItemStack> equipment = Lists.newArrayList();

            for (ItemStack stack : mc.player.getArmorInventoryList()) {
                equipment.add(stack != null ? stack.copy() : stack);
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
        return stack != null
                && (stack.getItem() instanceof ItemArmor
                || stack.getItem() instanceof ItemElytra);
    }

    private static void playEquipSound(ItemStack stack, EntityPlayer player) {
        Item item = stack.getItem();
        SoundEvent sound = null;

        if (item instanceof ItemArmor) {
            sound = ((ItemArmor) item).getArmorMaterial().getSoundEvent();
        } else if (item instanceof ItemElytra) {
            sound = soundElytraEquip;
        }

        if (sound != null) {
            player.world.playSound(player, new BlockPos(player), sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }

}
